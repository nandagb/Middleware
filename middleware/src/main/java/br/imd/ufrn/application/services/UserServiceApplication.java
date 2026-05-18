package br.imd.ufrn.application.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import br.imd.ufrn.Middleware;

public class UserServiceApplication {

    public static void main( String[] args ) {

        if (args.length == 0) {
            System.out.println("Erro! Nenhum argumento fornecido");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String protocol = args[1];

        Middleware middleware = new Middleware();
        middleware.register(UserService.class);

        new Thread( () -> sendHeartBeat(port)).start();

        middleware.start(port, protocol);
    }

    public static void sendHeartBeat(int port) {
        HttpClient  httpClient;

        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

        while(true) {
                System.out.println("Enviando Hearbeat");
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + "127.0.0.1" + ":" + 8080 + "/users/heartbeat?address=127.0.0.1&port=" + port))
                    .timeout(Duration.ofSeconds(2))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

                try {
                    httpClient.send(req, HttpResponse.BodyHandlers.discarding());
                    System.out.println("heartBeat enviado!");
                } catch (IOException e) {
                    System.out.println("IOException: erro ao enviar HeartBeat do User Service");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException: erro ao enviar HeartBeat  do User Service");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException: ao chamar sleep no UserService");
                    e.printStackTrace();
                }
            }
    }
}
