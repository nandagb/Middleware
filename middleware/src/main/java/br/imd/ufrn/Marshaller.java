package br.imd.ufrn;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.imd.ufrn.Annotations.Body;
import br.imd.ufrn.Annotations.Param;
import br.imd.ufrn.Exceptions.MarshalException;
import br.imd.ufrn.HTTP.HTTPRequest;

public class Marshaller {
    private ObjectMapper objectMapper;

    public Marshaller() {
        this.objectMapper = new ObjectMapper();
    }

    public Object[] unmarshallRequestParams(Method method, HTTPRequest request) throws MarshalException {
        Parameter[] methodParameters = method.getParameters();
        Object[] args = new Object[methodParameters.length];
        
        System.out.println("Method parameters lenght: " + methodParameters.length);
        for(int i=0; i < methodParameters.length; i++) {
            Parameter methodParameter = methodParameters[i];
            if (methodParameter.isAnnotationPresent(Param.class)) {
                Param annotation = methodParameter.getAnnotation(Param.class);
                String paramName = annotation.value();
                Class<?> type = methodParameter.getType();
                String valueString = request.getQueryParam(paramName);
                args[i] = getObjectValue(valueString, type);
            }
            else if (methodParameter.isAnnotationPresent(Body.class)){
                String body = request.getBody();
                Class<?> type = methodParameter.getType();
                try {
                    args[i] = objectMapper.readValue(body, type);
                } catch (JsonMappingException e) {
                    throw new MarshalException("JsonMappingException: Erro ao converter objeto JSON em Objeto Java", 400);
                } catch (JsonProcessingException e) {
                    throw new MarshalException("JsonProcessingException: Erro ao converter objeto JSON em Objeto Java", 400);
                }
            }
        }

        return args;
    }

    public String marshallBody(Object object) throws MarshalException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new MarshalException("JsonProcessingException: Erro ao converter objeto em string JSON", 500);
        }
    }

    private Object getObjectValue(String value, Class<?> type) {
        if (type == int.class ||
            type == Integer.class) {

            return Integer.parseInt(value);
        }

        if (type == double.class ||
            type == Double.class) {

            return Double.parseDouble(value);
        }

        if (type == boolean.class ||
            type == Boolean.class) {

            return Boolean.parseBoolean(value);
        }

        if (type == String.class) {
            return value;
        }

        return null;
    }
    
}
