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
import br.imd.ufrn.application.models.Message;
import br.imd.ufrn.application.models.ServiceRecord;

@Singleton
@RemoteService("/messages")
public class MessagesGatewayController {
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

    public MessagesGatewayController() {
        this.registry = new GatewayRegistry();
        this.protocol = MiddlewareConfig.getProtocol();
    }

    @Post("/heartbeat")
    public void listenHeartBeat(@Param("address") String stringAddress, @Param("port") int port)  {
        InetAddress address;

        try {
            address = InetAddress.getByName(stringAddress);
        } catch (UnknownHostException e) {
            System.out.println("Erro para inicializar o endereco no listenHearBeat do MessagesGateway: " + e);
            return;
        }

        ServiceRecord service = new ServiceRecord(address, port);
        String key = address + ":" + port;
        registry.update(key, service);
    }

    @Post("/send")
    public Message send(@Body Message message) throws Exception {
        System.out.println("Executando o send dentro do Messages gateway, com protocolo: " + this.protocol);
        // System.out.println("MessageService: de=" + senderId + " para=" + receiverId + " conteudo=" + content);
        String body;
        try {
            body = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RemoteException(
                "MessageService Gateway error: Erro interno ao processar requisicao",
                500
            );
        }

        ServiceRecord service = this.registry.getNextService();

        if (service == null) {
            System.out.println("MessageService Gateway error:  Nao ha servidores disponiveis!");
            throw new RemoteException(
                "MessageService Gateway error:  Nao ha servidores disponiveis!",
                503
            );
        }

        System.out.println("Enviando requisicao para porta: " + service.getPort());

        if (this.protocol.equals("tcp")) {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + "127.0.0.1" + ":" + service.getPort() + "/messages/send"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            try {
                HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

                Message responseMessage = objectMapper.readValue(res.body(), Message.class);

                return responseMessage;
            } catch (Exception e) {
                throw new RemoteException(
                    "MessageService Gateway error: Erro ao comunicar com UserService " + e,
                    502
                );
            }
        }
        else {
            System.out.println("Enviando requisicao para o servidor!");
            return null;
        }
    }
}
