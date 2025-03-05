package backend.academy.bot;

import backend.academy.DTO;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bot")
public class BotController {
    private final BotService botService;
    private static final Logger LOGGER = Logger.getLogger(BotController.class.getName());

    @Autowired
    public BotController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping("/updates")
    public void updates(@RequestBody DTO.LinkUpdate linkUpdate) {
        LOGGER.info("Получено обновление для отправки пользователям: " + linkUpdate);
        List<Long> tgIds = linkUpdate.tgChatIds();
        for (long id : tgIds) {
            LOGGER.info("Отправка пользователю " + id + " " + linkUpdate.description());
            botService.sendUpdate(id, linkUpdate.url());
        }
    }
}
