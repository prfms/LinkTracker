package backend.academy.scrapper;

import backend.academy.DTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BotNotifier {
    private final RestClient restClient;

    public BotNotifier() {
        this.restClient = RestClient.create("http://localhost:8080/api/bot/");
    }

    public DTO.LinkUpdate sendUpdate(DTO.LinkUpdate update) {
        return restClient
            .post()
            .uri("/updates")
            .body(update)
            .retrieve()
            .body(DTO.LinkUpdate.class);
    }
}
