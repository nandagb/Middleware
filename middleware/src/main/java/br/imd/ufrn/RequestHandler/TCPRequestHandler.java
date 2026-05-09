package br.imd.ufrn.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import br.imd.ufrn.HTTP.HTTPMarshaller;
import br.imd.ufrn.HTTP.HTTPRequest;
import br.imd.ufrn.HTTP.HTTPResponse;
import br.imd.ufrn.Invoker.Invoker;
import br.imd.ufrn.Invoker.LookupService;

public class TCPRequestHandler {
    int port;
    private ServerSocket serverSocket;
    private HTTPMarshaller marshaller;
    private Invoker invoker;

    public TCPRequestHandler(int port, Invoker invoker) {
        this.port = port;
        this.marshaller = new HTTPMarshaller();
        this.invoker = invoker;
    }

    private void processRequest(Socket connection) {
        System.out.println("Conexão aceita!");
        
        BufferedReader clientRequest = null;
        PrintWriter serverResponse = null;

        try {
            clientRequest = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            System.out.println("Erro ao ler a mensagem do cliente: IOException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        HTTPRequest request = this.marshaller.getHTTPRequest(clientRequest);

        if (request == null) {
            System.out.println("Não foi possível processar a requisição!");
            return;
        }

        System.out.println("Requisição recebida " + request.toString());
        String responseBody = invoker.invoke(request);

        HTTPResponse response = marshaller.getServiceResponse(responseBody);
        // ================================================================

        if (response == null) {
            System.out.println("Não foi possível processar a resposta!");
            return;
        }

        try {
            serverResponse = new PrintWriter(connection.getOutputStream());
        } catch (IOException e) {
            // e.printStackTrace();
            return;
        }

        serverResponse.println(response.getStatusLine());
        serverResponse.println(response.getHeaders());

        if (response.getContentLength() > 0 ) {
            serverResponse.print(response.getBody());
        }

        serverResponse.flush();
    }

    public void start() {
        System.out.println("Handler iniciado");
        try {
            this.serverSocket = new ServerSocket(this.port);

            while(true) {
                System.out.println("TCP Request Handler esperando conexão na porta " + this.port + "...");
                Socket connection = serverSocket.accept();

                processRequest(connection);
            }
        } catch (IOException e) {
            System.out.println("Erro ao criar o serverSocket do TCPRequestHandler");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
