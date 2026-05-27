package br.imd.ufrn.Interceptor;

import java.util.HashMap;
import java.util.Map;

import br.imd.ufrn.ResponseMessage;
import br.imd.ufrn.HTTP.HTTPRequest;
import br.imd.ufrn.HTTP.HTTPResponse;

public class InvocationContext {
    HTTPRequest request;
    ResponseMessage response;
    private Map<String, Object> attributes;

    public InvocationContext() {
        this.attributes = new HashMap<>();
    }

    public InvocationContext(HTTPRequest request) {
        this.request = request;
        this.attributes = new HashMap<>();
    }

    public void setRequest(HTTPRequest request) {
        this.request = request;
    }

    public void setResponse(ResponseMessage response) {
        this.response = response;
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public HTTPRequest getRequest() {
        return this.request;
    }

    public ResponseMessage getResponse() {
        return this.response;
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }
}
