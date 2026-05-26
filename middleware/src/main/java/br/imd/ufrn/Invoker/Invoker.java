package br.imd.ufrn.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import br.imd.ufrn.ResponseMessage;
import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.Exceptions.LifecycleException;
import br.imd.ufrn.Exceptions.LookupException;
import br.imd.ufrn.Exceptions.MarshalException;
import br.imd.ufrn.Exceptions.RemoteException;
import br.imd.ufrn.HTTP.HTTPRequest;
import br.imd.ufrn.Marshaller.Marshaller;

public class Invoker {
    private LifecycleManager lifecycleManager;
    private LookupService lookup;
    private Marshaller marshaller;

    public Invoker(LookupService lookup) {
        this.lifecycleManager = new LifecycleManager();
        this.marshaller = new Marshaller();
        this.lookup = lookup;
    }

    public ResponseMessage invoke(HTTPRequest request) {
        String HTTPMethod = request.getMethod();
        String resource = request.getResource();
        String route = request.getRoute();

        Class<?> serviceClass;
        Object remoteObject;
        Method method;
        Object[] args;
        try {
            serviceClass = lookup.getServiceClass(resource);
            remoteObject = lifecycleManager.getInstance(serviceClass);
            method = lookup.getMethod(serviceClass, HTTPMethod, route);
            args = marshaller.unmarshallRequestParams(method, request);
        } catch (RemoteException e) {
            System.out.println("RemoteException: " + e.getCode() + " " + e.getMessage());
            return new ResponseMessage(e.getMessage(), e.getCode());
        }

        try {
            Object result = method.invoke(remoteObject, args);
            String parsedResult = marshaller.marshallBody(result);
            return new ResponseMessage(parsedResult, 200);
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException: " + e);
            return new ResponseMessage("IllegalAccessException: Nao foi possivel invocar o metodo" + method.getName() + " da classe " + serviceClass.getName() + " remotamente", 500);
        } catch (InvocationTargetException e) {
            System.out.println("InvocationTargetException! " + e.getMessage());
            Throwable cause = e.getCause();

            if(cause instanceof RemoteException) {

                RemoteException remoteError = (RemoteException) cause;

                System.out.println("RemoteException from InvocationTargetException: " + remoteError.getCode() + " " + remoteError.getMessage());
                return new ResponseMessage(remoteError.getMessage(), remoteError.getCode());
            }
            else {
                System.out.println("InvocationTargetException: Nao foi possivel invocar o metodo" + method.getName() + " da classe " + serviceClass.getName() + " remotamente");
                return new ResponseMessage("InvocationTargetException: Nao foi possivel invocar o metodo" + method.getName() + " da classe " + serviceClass.getName() + " remotamente", 500);
            }
        } catch (RemoteException e) {
            System.out.println("RemoteException: " + e);
            return new ResponseMessage(e.getMessage(), e.getCode());
        } finally {
            lifecycleManager.releaseInstance(serviceClass, remoteObject);
        }
    }
    
}
