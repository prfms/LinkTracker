package backend.academy.bot;


import backend.academy.DTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/bot")
public class BotController {
    private final BotService botService;

    @Autowired
    public BotController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping("/updates")
    public void updates(@RequestBody DTO.LinkUpdate linkUpdate){
        List<Long> tgIds = linkUpdate.tgChatIds();
        for (long id : tgIds) {
            botService.sendUpdate(id, linkUpdate.description());
        }
    }
}
