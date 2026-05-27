package br.imd.ufrn;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Interceptor.Interceptor;
import br.imd.ufrn.Interceptor.InterceptorRegistry;
import br.imd.ufrn.Invoker.Invoker;
import br.imd.ufrn.Invoker.LookupService;
import br.imd.ufrn.RequestHandler.RequestHandler;
import br.imd.ufrn.RequestHandler.RequestHandlerFactory;
import br.imd.ufrn.RequestHandler.TCPRequestHandler;

public class Middleware 
{
    private LookupService lookup;
    private InterceptorRegistry interceptorRegistry;
    public static void main( String[] args ) {
        // System.out.println( "Hello World!" );
    }

    public void start(int port, String protocol) {
        System.out.println("Middleware iniciado");
        Invoker invoker = new Invoker(lookup, interceptorRegistry);
        RequestHandler handler = RequestHandlerFactory.createHandler(protocol, port, invoker);
        handler.start();
    }

    public Middleware() {
        this.lookup = new LookupService();
        this.interceptorRegistry = new InterceptorRegistry();
    }

    public void register(Class<?> serviceClass) {
        lookup.register(serviceClass);
    }

    public void registerInterceptor(Interceptor interceptor) {
        this.interceptorRegistry.registerInterceptor(interceptor);
    }
}
