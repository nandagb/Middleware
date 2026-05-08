package br.imd.ufrn.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.HTTP.HTTPRequest;

public class Invoker {
    private LifecycleManager lifecycleManager;
    private LookupService lookup;

    public Invoker(LookupService lookup) {
        this.lifecycleManager = new LifecycleManager();
        this.lookup = lookup;
    }

    public void invoke(HTTPRequest request) {
        String HTTPMethod = request.getMethod();
        String resource = request.getResource();
        String route = request.getRoute();
        Class<?> serviceClass = lookup.getServiceClass(resource);
        Object remoteObject = lifecycleManager.getInstance(serviceClass);
        Method method = lookup.getMethod(serviceClass, HTTPMethod, route);

        try {
            var result = method.invoke(remoteObject);
        } catch (IllegalAccessException e) {
            System.out.println("Erro ao invocar o método " + method.getName() + " da classe " + serviceClass.getName() + ": IllegalAccessException");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println("Erro ao invocar o método " + method.getName() + " da classe " + serviceClass.getName() + ": InvocationTargetException");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        lifecycleManager.releaseInstance(serviceClass, remoteObject);
    }
    
}
