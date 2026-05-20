package br.imd.ufrn.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HTTPMarshaller {
    private String protocol = "HTTP/1.1";

    public HTTPRequest getHTTPRequest(BufferedReader clientRequest) {
        StringBuilder headersBuilder = new StringBuilder();
        String requestLine;

        try {
            requestLine = clientRequest.readLine();
            HTTPRequest request = new HTTPRequest(requestLine);

            String line;
            while ((line = clientRequest.readLine()) != null && !line.isEmpty()) {
                headersBuilder.append(line).append("\r\n");
                if (line.startsWith("Content-Length:")) {
                    request.setContentLength(line);
                }
            }

            request.setHeaders(headersBuilder.toString());

            // if (request.getContentLength() > 0) {
            //     char[] body = new char[request.getContentLength()];
            //     clientRequest.read(body, 0, request.getContentLength());
            //     request.setBody(body);
            // }

            if (request.getContentLength() > 0) {
                char[] body = new char[request.getContentLength()];
                int totalRead = 0;

                while (totalRead < request.getContentLength()) {
                    int read = clientRequest.read(
                        body,
                        totalRead,
                        request.getContentLength() - totalRead
                    );

                    if (read == -1) {
                        break;
                    }

                    totalRead += read;
                }

                request.setBody(body);
            }

            return request;

        } catch (IOException e) {
            System.out.println("Erro ao criar requisição HTTP a partir do BufferedReader");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public HTTPResponse getHTTPResponse(String responseBody, int code){
	  	String status = HTTPUtils.mapStatus(code);
	  	String contentType = "application/json";
	  	String body = responseBody;
	  	int contentLength = body.length();
        // int contentLength = body.getBytes(StandardCharsets.UTF_8).length;

	  	StringBuilder headersBuilder = new StringBuilder();
		
	  	headersBuilder.append(this.protocol + " " + code + " " + status).append("\r\n");
	  	HTTPResponse response = new HTTPResponse(this.protocol, code, status);

	  	headersBuilder.append("Connection: close").append("\r\n");
        headersBuilder.append("Host: localhost").append("\r\n");
        headersBuilder.append("Content-Type: " + contentType).append("\r\n");
	  	headersBuilder.append("Content-Length: " + contentLength).append("\r\n");

	  	response.setHeaders(headersBuilder.toString());
	  	response.setContentLength(contentLength);
	  	response.setBody(body);

	  	return response;
   }

    public HTTPResponse getServiceResponse(String responseBody){
        int code = 200;
	  	String status = HTTPUtils.mapStatus(code);
	  	String contentType = "application/json";
	  	String body = responseBody;
	  	int contentLength = body.length();
	  	StringBuilder headersBuilder = new StringBuilder();
		
	  	headersBuilder.append(this.protocol + " " + code + " " + status).append("\r\n");
	  	HTTPResponse response = new HTTPResponse(this.protocol, code, status);

	  	headersBuilder.append("Content-Type: " + contentType).append("\r\n");
	  	headersBuilder.append("Content-Length: " + contentLength).append("\r\n");

	  	response.setHeaders(headersBuilder.toString());
	  	response.setContentLength(contentLength);
	  	response.setBody(body);

	  	return response;
   }
}
