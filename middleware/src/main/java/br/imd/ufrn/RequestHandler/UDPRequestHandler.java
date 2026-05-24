package br.imd.ufrn.RequestHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.imd.ufrn.HTTP.HTTPMarshaller;
import br.imd.ufrn.Invoker.Invoker;

public class UDPRequestHandler implements RequestHandler {
    private final int port;
    private final Invoker invoker;
    private final HTTPMarshaller marshaller = new HTTPMarshaller();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public UDPRequestHandler(int port, Invoker invoker) {
        this.port = port;
        this.invoker = invoker;
    }

    @Override
    public void start() {

    }
}
