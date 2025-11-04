package backend.academy.scrapper.scheduler;

import java.util.logging.Logger;
import backend.academy.scrapper.controller.dto.LinkUpdateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BotNotifier {
    private final RestClient restClient;
    private static final Logger LOGGER = Logger.getLogger(BotNotifier.class.getName());

    public BotNotifier(@Value("${bot.url}") String botUrl,
                       @Value("${bot.port}") String botPort) {
        this.restClient = RestClient.builder()
            .baseUrl("http://" + botUrl + ":" + botPort + "/api/bot/")
            .build();
    }

    public LinkUpdateDto sendUpdate(LinkUpdateDto update) {
        LOGGER.info("Отправка сообщения в бота: " + update);
        try {
            return restClient.post().uri("/updates").body(update).retrieve().body(LinkUpdateDto.class);
        } catch (Exception e) {
            LOGGER.warning("Ошибка при отправке запроса в бота: " + e.getMessage());
            return null;
        }
    }
}
