package backend.academy.bot.client;

import backend.academy.bot.client.dto.AddLinkRequestDto;
import backend.academy.bot.client.dto.LinkResponseDto;
import backend.academy.bot.client.dto.ListLinksResponseDto;
import backend.academy.bot.client.dto.RemoveLinkRequestDto;
import backend.academy.bot.client.dto.UserDto;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ScrapperClient {
    private final RestClient restClient;

    public ScrapperClient() {
        this.restClient = RestClient
            .builder()
            .baseUrl("http://localhost:8081/api/scrapper/")
            .build();

    }

    public UserDto registerUser(Long id) {
        return restClient
            .post()
            .uri("tg-chat/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(UserDto.class);
    }

    public UserDto deleteUser(Long id) {
        return restClient
                .delete()
                .uri(uriBuilder ->
                        uriBuilder.path("/tg-chat/{id}").queryParam("id", id).build(id))
                .retrieve()
                .body(UserDto.class);
    }

    public ListLinksResponseDto getLinks(Long chatId) {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponseDto.class);
    }

    public LinkResponseDto addLink(Long chatId, AddLinkRequestDto linkRequest) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkRequest)
                .retrieve()
                .body(LinkResponseDto.class);
    }

    public LinkResponseDto deleteLink(Long chatId, RemoveLinkRequestDto linkRequest) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(linkRequest)
                .retrieve()
                .body(LinkResponseDto.class);
    }
}
