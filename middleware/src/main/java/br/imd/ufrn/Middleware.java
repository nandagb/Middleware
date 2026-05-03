package br.imd.ufrn;

import java.net.ServerSocket;
import java.net.Socket;

import br.imd.ufrn.Invoker.Invoker;
import br.imd.ufrn.RequestHandler.TCPRequestHandler;

public class Middleware 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Invoker invoker = new Invoker();
        TCPRequestHandler handler = new TCPRequestHandler(8080, invoker);
        handler.start();
    }
}
