package backend.academy.scrapper;

import backend.academy.DTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.logging.Logger;

@Service
public class BotNotifier {
    private final RestClient restClient;
    private static final Logger LOGGER = Logger.getLogger(BotNotifier.class.getName());
    public BotNotifier() {
        this.restClient = RestClient.create("http://localhost:8080/api/bot/");
    }

    public DTO.LinkUpdate sendUpdate(DTO.LinkUpdate update) {
        LOGGER.info("Отправка сообщения в бота: " + update);
        try {
            return restClient
                .post()
                .uri("/updates")
                .body(update)
                .retrieve()
                .body(DTO.LinkUpdate.class);
        } catch (Exception e) {
            LOGGER.warning("Ошибка при отправке запроса в бота: " + e.getMessage());
            return null;
        }
    }
}
