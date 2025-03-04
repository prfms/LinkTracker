package backend.academy.scrapper;

import backend.academy.DTO;
import backend.academy.scrapper.model.Link;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scrapper")
public class ScrapperController {
    private final ScrapperService scrapperService;

    public ScrapperController(ScrapperService scrapperService) {
        this.scrapperService = scrapperService;
    }

    @PostMapping("/tg-chat/{id}")
    public DTO.User registerUser(@PathVariable Long id) {
        long userId = scrapperService.registerUser(id);
        return new DTO.User(userId);
    }

    @DeleteMapping("/tg-chat/{id}")
    public DTO.User deleteUser(@RequestBody @PathVariable Long id) {
        long userId = scrapperService.deleteUser(id);
        return new DTO.User(userId);
    }

    @GetMapping("/links")
    public ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        if (chatId == null || chatId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new DTO.ApiErrorResponse(
                            "Некорректные параметры запроса",
                            "400",
                            "BadRequestException",
                            "ID должен быть положительным",
                            List.of())); // ?
        }

        List<DTO.LinkResponse> links = scrapperService.getLinks(chatId);
        return ResponseEntity.ok(links.stream()
                .map(link -> new DTO.LinkResponse(link.id(), link.url(), link.tags(), link.filters()))
                .toList());
    }

    @PostMapping("/links")
    public ResponseEntity<?> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody DTO.AddLinkRequest link) {
        if (chatId == null || chatId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new DTO.ApiErrorResponse(
                            "Некорректные параметры запроса",
                            "400",
                            "BadRequestException",
                            "ID должен быть положительным",
                            List.of())); // ?
        }
        int id = scrapperService.addLink(chatId, link);

        return ResponseEntity.ok(new DTO.LinkResponse(id, link.link(), link.tags(), link.filters()));
    }

    @DeleteMapping("/links")
    public ResponseEntity<?> deleteLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody DTO.RemoveLinkRequest link) {
        if (chatId == null || chatId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new DTO.ApiErrorResponse(
                            "Некорректные параметры запроса",
                            "400",
                            "BadRequestException",
                            "ID должен быть положительным",
                            List.of())); // ?
        }
        Link responseLink = scrapperService.deleteLink(chatId, link);
        if (responseLink == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DTO.ApiErrorResponse(
                            "Ссылка не найдена",
                            "404",
                            "NotFoundException",
                            "Ссылка должны быть добавлена перед удалением",
                            List.of())); // ?
        } else {
            return ResponseEntity.ok(new DTO.LinkResponse(
                    responseLink.id(), responseLink.url(), responseLink.tags(), responseLink.filters()));
        }
    }
}
