package br.imd.ufrn.HTTP;

import java.io.BufferedReader;
import java.io.IOException;

public class HTTPMarshaller {
    public HTTPRequest getHTTPRequest(BufferedReader clientRequest) {
        StringBuilder headersBuilder = new StringBuilder();
        String firstHeader;

        try {
            firstHeader = clientRequest.readLine();
            HTTPRequest request = new HTTPRequest(firstHeader);

            String line;
            while ((line = clientRequest.readLine()) != null && !line.isEmpty()) {
                headersBuilder.append(line).append("\r\n");
                if (line.startsWith("Content-Length:")) {
                    request.setContentLength(line);
                }
            }
            request.setHeaders(headersBuilder.toString());
            if (request.getContentLength() > 0) {
                char[] body = new char[request.getContentLength()];
                clientRequest.read(body, 0, request.getContentLength());
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
}
