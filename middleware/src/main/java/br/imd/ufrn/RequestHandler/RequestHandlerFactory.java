package br.imd.ufrn.RequestHandler;

import br.imd.ufrn.Invoker.Invoker;

public class RequestHandlerFactory {
    public static RequestHandler createHandler(String protocol, int port, Invoker invoker) {
        switch (protocol) {
            case "tcp":
                return new TCPRequestHandler(port, invoker);
            case "udp":
                return new UDPRequestHandler(port, invoker);
            default:
                throw new IllegalArgumentException("Protocolo não implementado: " + protocol);
        }
    }
}
