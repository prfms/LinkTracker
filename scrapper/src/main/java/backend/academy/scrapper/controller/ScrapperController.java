package backend.academy.scrapper.controller;

import backend.academy.DTO;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.ScrapperService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class ScrapperController {
    private final ScrapperService scrapperService;

    public ScrapperController(ScrapperService scrapperService) {
        this.scrapperService = scrapperService;
    }

    @PostMapping("/tg-chat/{id}")
    public DTO.User registerUser(@PathVariable @Min(1) long id) {
        long userId = scrapperService.registerUser(id);
        return new DTO.User(userId);
    }

    @DeleteMapping("/tg-chat/{id}")
    public DTO.User deleteUser(@PathVariable @Min(1) long id) {
        long userId = scrapperService.deleteUser(id);
        return new DTO.User(userId);
    }

    @GetMapping("/links")
    public ResponseEntity<DTO.ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") @Min(1) long chatId) {
        List<DTO.LinkResponse> links = scrapperService.getLinks(chatId).stream()
                .map(link -> new DTO.LinkResponse(link.id(), link.url(), link.tags(), link.filters()))
                .toList();
        return ResponseEntity.ok(new DTO.ListLinksResponse(links, links.size()));
    }

    @PostMapping("/links")
    public ResponseEntity<DTO.LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") @Min(1) long chatId, @Valid @RequestBody DTO.AddLinkRequest link) {
        int id = scrapperService.addLink(chatId, link);
        return ResponseEntity.ok(new DTO.LinkResponse(id, link.link(), link.tags(), link.filters()));
    }

    @DeleteMapping("/links")
    public ResponseEntity<DTO.LinkResponse> deleteLink(
            @RequestHeader("Tg-Chat-Id") @Min(1) long chatId, @Valid @RequestBody DTO.RemoveLinkRequest link) {
        Link responseLink = scrapperService.deleteLink(chatId, link);
        if (responseLink == null) {
            throw new NoSuchElementException("Ссылка должна быть добавлена перед удалением");
        }
        return ResponseEntity.ok(new DTO.LinkResponse(
                responseLink.id(), responseLink.url(), responseLink.tags(), responseLink.filters()));
    }
}
