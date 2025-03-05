package backend.academy.bot;

import backend.academy.DTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ScrapperClient {
    private final RestClient restClient;

    public ScrapperClient() {
        this.restClient = RestClient.create("http://localhost:8081/api/scrapper/");
    }

    public DTO.User registerUser(Long id) {
        return restClient.post().uri("tg-chat/{id}", id).retrieve().body(DTO.User.class);
    }

    public DTO.User deleteUser(Long id) {
        return restClient
                .delete()
                .uri(uriBuilder ->
                        uriBuilder.path("/tg-chat/{id}").queryParam("id", id).build(id))
                .retrieve()
                .body(DTO.User.class);
    }

    public DTO.ListLinksResponse getLinks(Long chatId) {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public DTO.LinkResponse addLink(Long chatId, DTO.AddLinkRequest linkRequest) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(linkRequest)
                .retrieve()
                .body(DTO.LinkResponse.class);
    }

    public DTO.LinkResponse deleteLink(Long chatId, DTO.RemoveLinkRequest linkRequest) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(linkRequest)
                .retrieve()
                .body(DTO.LinkResponse.class);
    }
}
