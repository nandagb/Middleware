package br.imd.ufrn.application.services;

import br.imd.ufrn.Middleware;

public class MessageServiceApplication {
    public static void main( String[] args ) {
        Middleware middleware = new Middleware();
        middleware.register(MessageService.class);
        middleware.start(9005, "tcp");
        // new Thread(new HeartbeatSender("127.0.0.1", 9007, service, port, 1000)).start();
    }
}
