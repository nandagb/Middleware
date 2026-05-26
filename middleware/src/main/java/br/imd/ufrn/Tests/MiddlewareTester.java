package br.imd.ufrn.Tests;

import br.imd.ufrn.Middleware;
// import br.imd.ufrn.application.services.MessageService;

public class MiddlewareTester {
    public static void main( String[] args ) {
        Middleware middleware = new Middleware();
        middleware.register(Service.class);
        // middleware.register(MessageService.class);
        middleware.start(9005, "tcp");
        // new Thread(new HeartbeatSender("127.0.0.1", 9007, service, port, 1000)).start();
    }
}
