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
import br.imd.ufrn.Invoker.LookupService;

public class TCPRequestHandler {
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
        // Keep-alive enabled so the client can reuse the connection after an error
        HTTPResponse response = marshaller.getHTTPResponse(message, code, true);

        try {
            PrintWriter gatewayResponse = new PrintWriter(connection.getOutputStream());

            gatewayResponse.println(response.getStatusLine());
            gatewayResponse.println(response.getHeaders());   // includes Content-Length
            gatewayResponse.println();                        // empty line separating headers from body
            if (response.getContentLength() > 0) {
                gatewayResponse.print(response.getBody());
            }
            gatewayResponse.flush(); // do not close the socket here
        } catch (IOException e) {
            System.out.println("Erro! Não foi possível retornar o erro para o cliente: código: " + code
                    + "\n mensagem: " + message);
        }
    }

    private void processRequest(Socket connection) {
        // System.out.println("Conexão aceita!");
        // System.out.println("INICIO CONEXÃO");

        BufferedReader clientRequest = null;
        PrintWriter serverResponse = null;

        try {
            // Set 5 seconds idle timeout so inactive connections are closed
            // Set infinite timeout to allow keep-alive connections without premature closure
            connection.setSoTimeout(0);

            try {
                clientRequest = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                handleRequestHandlerError(connection, 400,
                        "{\"error\": \"BadRequest: Não foi possível ler a mensagem do cliente!\"}");
                return;
            }

            try {
                serverResponse = new PrintWriter(connection.getOutputStream());
            } catch (IOException e) {
                handleRequestHandlerError(connection, 500,
                        "{\"error\": \"InternalServerError: IOException: Não foi possível obter o output stream do servidor!\"}");
                return;
            }

            while (true) {
                HTTPRequest request = this.marshaller.getHTTPRequest(clientRequest);

                if (request == null) {
                    // Connection closed by client or EOF reached
                    break;
                }

                System.out.println("received request\n" + request.toString());

                // Process the request
                ResponseMessage responseMessage = invoker.invoke(request);

                // Determine if we should keep connection alive (HTTP/1.1 is keep-alive by
                // default)
                String connectionHeader = request.getHeader("Connection");
                boolean keepAlive = true;
                if (connectionHeader != null && connectionHeader.equalsIgnoreCase("close")) {
                    keepAlive = false;
                }

                HTTPResponse response = marshaller.getHTTPResponse(responseMessage.getMessage(),
                        responseMessage.getCode(), keepAlive);

                if (response == null) {
                    handleRequestHandlerError(connection, 500,
                            "{\"error\": \"InternalServerError: Não foi possível processar a resposta!\"}");
                    continue;
                }

                serverResponse.println(response.getStatusLine());
                serverResponse.println(response.getHeaders());
                serverResponse.println();

                if (response.getContentLength() > 0) {
                    serverResponse.print(response.getBody());
                }

                serverResponse.flush();
                if (!keepAlive) {
                    break;
                }
                // When keep-alive is true, set a reasonable read timeout for the next request
                // (e.g., 30 seconds). This prevents the thread from blocking forever if the
                // client
                // stays idle.
                if (keepAlive) {
                    connection.setSoTimeout(30_000); // 30 s
                }
            }
        } catch (Exception e) {
            handleRequestHandlerError(connection, 500,
                    "{\"error\": \"InternalServerError: Houve algum problema desconhecido!\"}");
        } finally {
            try {
                if (clientRequest != null)
                    clientRequest.close();
            } catch (Exception ignored) {
            }
            try {
                if (serverResponse != null)
                    serverResponse.close();
            } catch (Exception ignored) {
            }
            try {
                connection.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void start() {
        System.out.println("Handler iniciado");
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            System.out.println("IOException: Erro ao criar o serverSocket do TCPRequestHandler");
            return;
        }

        while (true) {
            // System.out.println("TCP Request Handler esperando conexão na porta " +
            // this.port + "...");
            Socket connection;

            try {
                connection = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("IOException: Erro! Não foi possível aceitar a conexão TCP em TCPRequestHandler");
                return;
            }

            executor.execute(() -> processRequest(connection));
        }
    }
}
