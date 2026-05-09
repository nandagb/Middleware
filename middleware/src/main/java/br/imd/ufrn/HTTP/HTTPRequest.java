package br.imd.ufrn.HTTP;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HTTPRequest {
    private String method;
    private String path;
    private String queryString;
    private String requestLine;
    private String resource;
    private String route;
    private Map<String, String> queryParams;


    private int contentLength;
    private String body;
    private Map<String, String> headers;

    public HTTPRequest() {
        this.contentLength = 0;
        queryParams = new HashMap<>();
        headers = new HashMap<>();
    }

    public HTTPRequest(String requestLine) {
        this.contentLength = 0;
        this.requestLine = requestLine;
        queryParams = new HashMap<>();
        headers = new HashMap<>();

        StringTokenizer tokenizer = new StringTokenizer(requestLine);
        this.method = tokenizer.nextToken();

        String pathQuery = tokenizer.nextToken();

        if (pathQuery.contains("?")) {
            String[] parts = pathQuery.split("\\?", 2);

            this.path = parts[0];
            this.queryString = parts[1];
            parseQueryParams(this.queryString);
        } else {
            this.path = pathQuery;
        }

        String[] pathParts = this.path.split("/");
        this.resource = "/" + pathParts[1];
        this.route = "/" + pathParts[2];
    }

    private void parseQueryParams(String query) {
        System.out.println("string query: " + query);
        String[] params = query.split("&");

        for (String param : params) {
            System.out.println("param inside parse: " + param);
            String[] keyValue = param.split("=", 2);

            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";

            this.queryParams.put(key, value);
        }
    }

    public Map<String, String> getQueryParams() {
        return this.queryParams;
    }

    public String getQueryParam(String paramName) {
        String paramValue = this.queryParams.get(paramName);
        System.out.println("paramValue: " + paramValue);
        return paramValue;
    }

    public String getRequestLine() {
        return this.requestLine;
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getResource() {
        return this.resource;
    }

    public String getRoute() {
        return this.route;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public void setContentLength(String line) {
        this.contentLength = Integer.parseInt(line.split(":")[1].trim());
    }

    public void setContentLength(int length) {
        this.contentLength = length;
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public void setHeaders(String headers) {
        String[] lines = headers.split("\r\n");

        for (String line : lines) {
            this.setHeader(line);
        }
    }

    public void setHeader(String header) {
        int index = header.indexOf(":");
        if (index > 0) {
            String headerKey = header.substring(0, index).trim();
            String value = header.substring(index + 1).trim();
            this.headers.put(headerKey, value);
        }
    }

    public String getHeaders() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey())
                   .append(": ")
                   .append(entry.getValue())
                   .append("\r\n");
        }

        return builder.toString();
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setBody(char[] body) {
        this.body = new String(body);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    public String toString() {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append(this.requestLine).append("\r\n");
        messageBuilder.append(this.getHeaders()).append("\r\n");

        if (this.contentLength > 0 ) {
            messageBuilder.append(this.body).append("\r\n");
        }

        String request = messageBuilder.toString();
        return request;
    }
}
