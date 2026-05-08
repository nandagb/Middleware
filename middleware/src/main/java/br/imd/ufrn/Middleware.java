package br.imd.ufrn;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Invoker.Invoker;
import br.imd.ufrn.Invoker.LookupService;
import br.imd.ufrn.RequestHandler.TCPRequestHandler;

public class Middleware 
{
    private LookupService lookup;
    public static void main( String[] args ) {
        // System.out.println( "Hello World!" );
    }

    public void start(int port) {
        System.out.println("Middleware iniciado");
        Invoker invoker = new Invoker(lookup);
        TCPRequestHandler handler = new TCPRequestHandler(port, invoker);
        handler.start();
    }

    public Middleware() {
        this.lookup = new LookupService();
    }

    public void register(Class<?> serviceClass) {
        lookup.register(serviceClass);
    }
}
