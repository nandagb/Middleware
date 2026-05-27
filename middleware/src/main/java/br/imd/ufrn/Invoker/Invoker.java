package br.imd.ufrn.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import br.imd.ufrn.ResponseMessage;
import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.Exceptions.LifecycleException;
import br.imd.ufrn.Exceptions.LookupException;
import br.imd.ufrn.Exceptions.MarshalException;
import br.imd.ufrn.Exceptions.RemoteException;
import br.imd.ufrn.HTTP.HTTPRequest;
import br.imd.ufrn.Interceptor.Interceptor;
import br.imd.ufrn.Interceptor.InterceptorRegistry;
import br.imd.ufrn.Interceptor.InvocationContext;
import br.imd.ufrn.Marshaller.Marshaller;

public class Invoker {
    private LifecycleManager lifecycleManager;
    private LookupService lookup;
    private Marshaller marshaller;
    private InterceptorRegistry interceptorRegistry;

    public Invoker(LookupService lookup, InterceptorRegistry interceptorRegistry ) {
        this.lifecycleManager = new LifecycleManager();
        this.marshaller = new Marshaller();
        this.lookup = lookup;
        this.interceptorRegistry = interceptorRegistry;
    }

    public ResponseMessage invoke(HTTPRequest request) {
        InvocationContext context = new InvocationContext(request);

        String HTTPMethod = request.getMethod();
        String resource = request.getResource();
        String route = request.getRoute();

        Class<?> serviceClass;
        Object remoteObject;
        Method method;
        Object[] args;

        /// BEFORE INTERCEPTORS
        for (Interceptor interceptor : this.interceptorRegistry.getInterceptors()) {
            try {
                interceptor.before(context);
            } catch (RemoteException e) {
                System.out.println("RemoteException do Interceptor: " + e.getCode() + " " + e.getMessage());
                return new ResponseMessage(e.getMessage(), e.getCode());
            }
        }

        ResponseMessage responseMessage = null;

        try {
            serviceClass = lookup.getServiceClass(resource);
            remoteObject = lifecycleManager.getInstance(serviceClass);
            method = lookup.getMethod(serviceClass, HTTPMethod, route);
            args = marshaller.unmarshallRequestParams(method, request);
        } catch (RemoteException e) {
            System.out.println("RemoteException: " + e.getCode() + " " + e.getMessage());
            responseMessage = new ResponseMessage(e.getMessage(), e.getCode());
            return responseMessage;
        }

        try {
            Object result = method.invoke(remoteObject, args);
            String parsedResult = marshaller.marshallBody(result);
            responseMessage = new ResponseMessage(parsedResult, 200);
            return responseMessage;
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException: " + e);
            responseMessage = new ResponseMessage("IllegalAccessException: Nao foi possivel invocar o metodo" + method.getName() + " da classe " + serviceClass.getName() + " remotamente", 500);
            return responseMessage;
        } catch (InvocationTargetException e) {
            System.out.println("InvocationTargetException! " + e.getMessage());
            Throwable cause = e.getCause();

            if(cause instanceof RemoteException) {

                RemoteException remoteError = (RemoteException) cause;

                System.out.println("RemoteException do InvocationTargetException: " + remoteError.getCode() + " " + remoteError.getMessage());
                responseMessage = new ResponseMessage(remoteError.getMessage(), remoteError.getCode());
                return responseMessage;
            }
            else {
                System.out.println("InvocationTargetException: Nao foi possivel invocar o metodo" + method.getName() + " da classe " + serviceClass.getName() + " remotamente");
                responseMessage = new ResponseMessage("InvocationTargetException: Nao foi possivel invocar o metodo" + method.getName() + " da classe " + serviceClass.getName() + " remotamente", 500);
                return responseMessage;
            }
        } catch (RemoteException e) {
            System.out.println("RemoteException: " + e);
            responseMessage = new ResponseMessage(e.getMessage(), e.getCode());
            return responseMessage;
        } finally {
            context.setResponse(responseMessage);

            /// AFTER INTERCEPTORS
            for (Interceptor interceptor : this.interceptorRegistry.getInterceptors()) {
                try {
                    interceptor.after(context);
                } catch (RemoteException e) {
                    System.out.println("RemoteException do Interceptor no After: " + e.getCode() + " " + e.getMessage());
                    return new ResponseMessage(e.getMessage(), e.getCode());
                }
            }

            lifecycleManager.releaseInstance(serviceClass, remoteObject);
        }
    }
    
}
