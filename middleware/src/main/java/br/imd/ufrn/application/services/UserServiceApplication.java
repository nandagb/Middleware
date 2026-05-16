package br.imd.ufrn.application.services;

import br.imd.ufrn.Middleware;

public class UserServiceApplication {
    public static void main( String[] args ) {
        Middleware middleware = new Middleware();
        middleware.register(UserService.class);
        middleware.start(9006, "tcp");
        // new Thread(new HeartbeatSender("127.0.0.1", 9007, service, port, 1000)).start();
    }
}
