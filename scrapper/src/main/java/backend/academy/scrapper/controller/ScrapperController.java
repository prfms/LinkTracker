package backend.academy.scrapper.controller;

import backend.academy.scrapper.controller.dto.AddLinkRequestDto;
import backend.academy.scrapper.controller.dto.LinkResponseDto;
import backend.academy.scrapper.controller.dto.ListLinksResponseDto;
import backend.academy.scrapper.controller.dto.RemoveLinkRequestDto;
import backend.academy.scrapper.controller.dto.UserDto;
import backend.academy.scrapper.model.LinkDto;
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
    public UserDto registerUser(@PathVariable @Min(1) long id) {
        long userId = scrapperService.registerUser(id);
        return new UserDto(userId);
    }

    @DeleteMapping("/tg-chat/{id}")
    public UserDto deleteUser(@PathVariable @Min(1) long id) {
        long userId = scrapperService.deleteUser(id);
        return new UserDto(userId);
    }

    @GetMapping("/links")
    public ResponseEntity<ListLinksResponseDto> getLinks(@RequestHeader("Tg-Chat-Id") @Min(1) long chatId) {
        List<LinkResponseDto> links = scrapperService.getLinks(chatId).stream()
                .map(link -> new LinkResponseDto(link.id(), link.url()))
                .toList();
        return ResponseEntity.ok(new ListLinksResponseDto(links, links.size()));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponseDto> addLink(
            @RequestHeader("Tg-Chat-Id") @Min(1) long chatId, @Valid @RequestBody AddLinkRequestDto link) {
        int id = scrapperService.addLink(chatId, link);
        return ResponseEntity.ok(new LinkResponseDto(id, link.link()));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponseDto> deleteLink(
            @RequestHeader("Tg-Chat-Id") @Min(1) long chatId, @Valid @RequestBody RemoveLinkRequestDto link) {
        LinkDto responseLink = scrapperService.deleteLink(chatId, link);
        if (responseLink == null) {
            throw new NoSuchElementException("Ссылка должна быть добавлена перед удалением");
        }
        return ResponseEntity.ok(new LinkResponseDto(
                responseLink.id(), responseLink.url()));
    }
}
