package br.imd.ufrn.application.gateway;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.imd.ufrn.Annotations.Body;
import br.imd.ufrn.Annotations.Param;
import br.imd.ufrn.Annotations.Post;
import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.Annotations.Singleton;
import br.imd.ufrn.Exceptions.RemoteException;
import br.imd.ufrn.application.models.ServiceRecord;
import br.imd.ufrn.application.models.User;

@Singleton
@RemoteService("/users")
public class UsersGatewayController {
    // private HttpClient httpClient;
    // private ObjectMapper objectMapper;
    private static final HttpClient httpClient =
    HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(2))
        .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private GatewayRegistry registry;
    private String protocol;

    public UsersGatewayController() {
        this.registry = new GatewayRegistry();
        this.protocol = MiddlewareConfig.getProtocol();
    }

    @Post("/heartbeat")
    public void listenHeartBeat(@Param("address") String stringAddress, @Param("port") int port)  {
        InetAddress address;

        try {
            address = InetAddress.getByName(stringAddress);
        } catch (UnknownHostException e) {
            System.out.println("Erro para inicializar o endereco no listenHearBeat do UsersGateway: " + e);
            return;
        }

        ServiceRecord service = new ServiceRecord(address, port);
        String key = address + ":" + port;
        registry.update(key, service);
    }

    @Post("/create")
    public User create(@Body User user) throws Exception {
        System.out.println("Executando o create dentro do Users gateway, com protocolo: " + this.protocol);
        // System.out.println("MessageService: de=" + senderId + " para=" + receiverId + " conteudo=" + content);
        String body;
        try {
            body = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RemoteException(
                "UserService Gateway error: Erro interno ao processar requisicao",
                500
            );
        }

        ServiceRecord service = this.registry.getNextService();

        if (service == null) {
            System.out.println("UserService Gateway error:  Nao ha servidores disponiveis!");
            throw new RemoteException(
                "UserService Gateway error:  Nao ha servidores disponiveis!",
                503
            );
        }

        System.out.println("Enviando requisicao para porta: " + service.getPort());

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://" + "127.0.0.1" + ":" + service.getPort() + "/users/create"))
            .timeout(Duration.ofSeconds(2))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        try {
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            // System.out.println("Resposta recebida do User Service: " + res.body());
                
            User responseUser = objectMapper.readValue(res.body(), User.class);

            return responseUser;
        } catch (Exception e) {
            throw new RemoteException(
                "UserService Gateway error: Erro ao comunicar com MessageService " + e,
                502
            );
        }
            
    }
}
