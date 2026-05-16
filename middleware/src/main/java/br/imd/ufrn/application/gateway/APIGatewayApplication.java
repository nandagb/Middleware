package br.imd.ufrn.application.gateway;

import br.imd.ufrn.Middleware;

public class APIGatewayApplication {
    public static void main( String[] args ) {
        Middleware middleware = new Middleware();
        //classe com anotações de messages
        middleware.register(MessagesGatewayController.class);
        //classe com anotações de user
        middleware.register(UsersGatewayController.class);
        middleware.start(8080, "tcp");
    }
}
