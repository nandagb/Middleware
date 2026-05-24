package br.imd.ufrn.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.imd.ufrn.ResponseMessage;
import br.imd.ufrn.HTTP.HTTPMarshaller;
import br.imd.ufrn.HTTP.HTTPRequest;
import br.imd.ufrn.HTTP.HTTPResponse;
import br.imd.ufrn.Invoker.Invoker;

public class UDPRequestHandler implements RequestHandler {
    private final int port;
    private final Invoker invoker;
    private final HTTPMarshaller marshaller = new HTTPMarshaller();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public UDPRequestHandler(int port, Invoker invoker) {
        this.port = port;
        this.invoker = invoker;
    }

    private DatagramPacket processRequest(DatagramPacket packet) {
        System.out.println("Conexao aceita!");

        String message = new String(packet.getData(), 0,       packet.getLength());
		BufferedReader messageReader = new BufferedReader(new StringReader(message));

		HTTPRequest request = this.marshaller.getHTTPRequest(messageReader);

		if (request == null) {
            System.out.println("Nao foi possivel processar a requisicao!");
			return null;
        }

        ResponseMessage responseMessage = invoker.invoke(request);
        HTTPResponse response = marshaller.getHTTPResponse(responseMessage.getMessage(), responseMessage.getCode());

        // response.setHeader("X-Client-IP" + ": " + request.getHeader("X-Client-IP"));
		// response.setHeader("X-Client-Port" + ": " + request.getHeader("X-Client-Port"));

        String reply = response.toString();
		byte[] replymsg = reply.getBytes();

        return new DatagramPacket(replymsg, replymsg.length, packet.getAddress(), packet.getPort());
    }

    @Override
    public void start() {
        DatagramSocket serverSocket;

        try {
            serverSocket = new DatagramSocket(this.port);
        } catch (SocketException e) {
            System.out.println("SocketException: Erro ao criar o serverSocket do UDPRequestHandler: " + e.getMessage());
            return;
        }

        while (true) {
            byte[] clientMessage = new byte[1024];
            DatagramPacket clientPacket = new DatagramPacket(clientMessage, clientMessage.length);
            try {
                serverSocket.receive(clientPacket);

                DatagramPacket packetCopy = new DatagramPacket(
                    clientPacket.getData().clone(),
                    clientPacket.getLength(),
                    clientPacket.getAddress(),
                    clientPacket.getPort()
                );

                executor.execute(() -> {
                    DatagramPacket serverPacket = processRequest(packetCopy);

                    if (serverPacket == null) {
                        System.out.println("Nao foi possivel processar o pacote!");
                    }
                    else {
                        try {
                            serverSocket.send(serverPacket);
                        } catch (IOException e) {
                            System.out.println("IOException: Nao foi possivel processar o pacote: " + e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                System.out.println("IOException: Erro! Nao foi possivel aceitar a conexao UDP em UDPRequestHandler: " + e.getMessage());
                return;
            }
        }

    }
}
