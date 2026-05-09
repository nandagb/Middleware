package br.imd.ufrn.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import br.imd.ufrn.Marshaller;
import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.HTTP.HTTPRequest;

public class Invoker {
    private LifecycleManager lifecycleManager;
    private LookupService lookup;
    private Marshaller marshaller;

    public Invoker(LookupService lookup) {
        this.lifecycleManager = new LifecycleManager();
        this.marshaller = new Marshaller();
        this.lookup = lookup;
    }

    public String invoke(HTTPRequest request) {
        String HTTPMethod = request.getMethod();
        String resource = request.getResource();
        String route = request.getRoute();

        Class<?> serviceClass = lookup.getServiceClass(resource);
        Object remoteObject = lifecycleManager.getInstance(serviceClass);
        Method method = lookup.getMethod(serviceClass, HTTPMethod, route);

        Object[] args = marshaller.unmarshallRequestParams(method, request);


        try {
            Object result = method.invoke(remoteObject, args);
            String parsedResult = marshaller.marshallBody(result);
            return parsedResult;
        } catch (IllegalAccessException e) {
            System.out.println("Erro ao invocar o método " + method.getName() + " da classe " + serviceClass.getName() + ": IllegalAccessException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Erro ao invocar o método " + method.getName() + " da classe " + serviceClass.getName() + ": IllegalAccessException";
        } catch (InvocationTargetException e) {
            System.out.println("Erro ao invocar o método " + method.getName() + " da classe " + serviceClass.getName() + ": InvocationTargetException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Erro ao invocar o método " + method.getName() + " da classe " + serviceClass.getName() + ": InvocationTargetException";
        } finally {
            lifecycleManager.releaseInstance(serviceClass, remoteObject);
        }
    }
    
}
