package br.imd.ufrn.application.gateway;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.imd.ufrn.Annotations.Body;
import br.imd.ufrn.Annotations.Post;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.application.models.User;

@RemoteService("/users")
public class UsersGatewayController {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    @Post("/create")
    public User create(@Body User user) {
        try {
            System.out.println("Executando o create dentro do gateway");
            // System.out.println("MessageService: de=" + senderId + " para=" + receiverId + " conteúdo=" + content);
            this.objectMapper = new ObjectMapper();
            String body = objectMapper.writeValueAsString(user);

            httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + "127.0.0.1" + ":" + 9006 + "/users/create"))
                // .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("Resposta recebida do User Service: " + res.body());
            
            User responseUser = objectMapper.readValue(res.body(), User.class);

            return responseUser;
        } catch (Exception e) {
            System.out.println("UserService Gateway error: " + e.getMessage());
            return null;
        }
    }
}
