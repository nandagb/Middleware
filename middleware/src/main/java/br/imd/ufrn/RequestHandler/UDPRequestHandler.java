package br.imd.ufrn.RequestHandler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.imd.ufrn.HTTP.HTTPMarshaller;
import br.imd.ufrn.HTTP.HTTPResponse;
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
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[8192];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                // Simple protocol: payload is the raw HTTP request line + headers + optional
                // body.
                // For now we reuse HTTPMarshaller to parse from a String.
                String payload = new String(packet.getData(), 0, packet.getLength(),
                        java.nio.charset.StandardCharsets.UTF_8);
                // Build a minimal HTTPRequest manually (could reuse existing parser).
                // For brevity we treat the whole payload as the body and use a fixed GET.
                // Real implementation can delegate to HTTPMarshaller if needed.
                // Here we just echo back a 200 OK with the received payload.
                HTTPResponse response = marshaller.getHTTPResponse("Received via UDP", 200);
                byte[] respBytes = response.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                DatagramPacket respPacket = new DatagramPacket(respBytes, respBytes.length,
                        packet.getAddress(), packet.getPort());
                socket.send(respPacket);
            }
        } catch (Exception e) {
            // Log and continue or terminate based on severity.
            System.out.println("UDPRequestHandler error: " + e.getMessage());
        }
    }
}
