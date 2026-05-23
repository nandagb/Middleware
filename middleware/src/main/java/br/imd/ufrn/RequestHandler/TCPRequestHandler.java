package br.imd.ufrn.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.imd.ufrn.ResponseMessage;
import br.imd.ufrn.HTTP.HTTPMarshaller;
import br.imd.ufrn.HTTP.HTTPRequest;
import br.imd.ufrn.HTTP.HTTPResponse;
import br.imd.ufrn.Invoker.Invoker;

public class TCPRequestHandler implements RequestHandler{
    int port;
    private ServerSocket serverSocket;
    private HTTPMarshaller marshaller;
    private Invoker invoker;
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public TCPRequestHandler(int port, Invoker invoker) {
        this.port = port;
        this.marshaller = new HTTPMarshaller();
        this.invoker = invoker;
    }

    public void handleRequestHandlerError(Socket connection, int code, String message) {
        HTTPResponse response = marshaller.getHTTPResponse(message, code);

        try {
            PrintWriter gatewayResponse = new PrintWriter(connection.getOutputStream());

            gatewayResponse.println(response.getStatusLine());
            gatewayResponse.println(response.getHeaders());

            if (response.getContentLength() > 0 ) {
                System.out.println("Erro retornado ao cliente: código: " + code  + "\n mensagem: " + response.getBody());
                gatewayResponse.print(response.getBody());
            }

            gatewayResponse.flush();
        } catch (IOException e) {
            System.out.println("Erro! Não foi possível retornar o erro para o cliente: código: " + code + "\n mensagem: " + message);
        }
    }

    private void processRequest(Socket connection) {
        // System.out.println("Conexão aceita!");
        // System.out.println("INICIO CONEXÃO");

        BufferedReader clientRequest = null;
        PrintWriter serverResponse = null;

        try {
            // Set 5 seconds idle timeout so inactive connections are closed
            connection.setSoTimeout(5000);

            try {
                clientRequest = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                handleRequestHandlerError(connection, 400, "{\"error\": \"BadRequest: Não foi possível ler a mensagem do cliente!\"}");
                return;
            }

            try {
                serverResponse = new PrintWriter(connection.getOutputStream());
            } catch (IOException e) {
                handleRequestHandlerError(connection, 500, "{\"error\": \"InternalServerError: IOException: Não foi possível obter o output stream do servidor!\"}");
                return;
            }

            while (true) {
                HTTPRequest request = this.marshaller.getHTTPRequest(clientRequest);

                if (request == null) {
                    break;
                }

                System.out.println("Requisição TCP com HTTP recebida\n" + request.toString());

                ResponseMessage responseMessage = invoker.invoke(request);

                // Determinar se a requisição deve manter keep alive (HTTP/1.1 é keep-alive por padrão)
                String connectionHeader = request.getHeader("Connection");
                boolean keepAlive = true;
                if (connectionHeader != null && connectionHeader.equalsIgnoreCase("close")) {
                    keepAlive = false;
                }

                HTTPResponse response = marshaller.getHTTPResponse(responseMessage.getMessage(), responseMessage.getCode(), keepAlive);

                if (response == null) {
                    handleRequestHandlerError(connection, 500, "{\"error\": \"InternalServerError: Não foi possível processar a resposta!\"}");
                    break;
                }

                serverResponse.println(response.getStatusLine());
                serverResponse.println(response.getHeaders());

                if (response.getContentLength() > 0 ) {
                    serverResponse.print(response.getBody());
                }

                serverResponse.flush();
                if (!keepAlive) {
                    break;
                }
            }
        } catch (Exception e) {
            handleRequestHandlerError(connection, 500, "{\"error\": \"InternalServerError: Houve algum problema desconhecido!\"}");
        }
         finally {
            try { if (clientRequest != null) clientRequest.close(); } catch (Exception ignored) {}
            try { if (serverResponse != null) serverResponse.close(); } catch (Exception ignored) {}
            try { connection.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void start() {
        System.out.println("Handler iniciado");
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            System.out.println("IOException: Erro ao criar o serverSocket do TCPRequestHandler");
            return;
        }

        while(true) {
            // System.out.println("TCP Request Handler esperando conexão na porta " + this.port + "...");
            Socket connection;

            try {
                connection = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("IOException: Erro! Não foi possível aceitar a conexão TCP em TCPRequestHandler");
                return;
            }

            executor.execute(() -> processRequest(connection)) ;
        }
    }
}
