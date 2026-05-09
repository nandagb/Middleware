package br.imd.ufrn;

public class MiddlewareTester {
    public static void main( String[] args ) {
        Middleware middleware = new Middleware();
        middleware.register(Service.class);
        middleware.start(8080);
    }
}
