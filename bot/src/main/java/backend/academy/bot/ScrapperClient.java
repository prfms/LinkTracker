package backend.academy.bot;

import backend.academy.DTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

@Service
public class ScrapperClient {
    private final RestClient restClient;

    public ScrapperClient() {
        this.restClient = RestClient.create("http://localhost:8081/api/scrapper/");
    }

    public DTO.User registerUser(Long id) {
        return restClient.post()
            .uri("tg-chat/{id}", id)  // Указываем путь с параметром id
            .retrieve()
            .body(DTO.User.class);  // Преобразуем ответ в DTO.User
    }

    public DTO.User deleteUser(Long id) {
        return restClient.delete()
            .uri(uriBuilder -> uriBuilder.path("/tg-chat/{id}").queryParam("id", id).build(id))
            .retrieve()
            .body(DTO.User.class);
    }

    public List<DTO.LinkResponse> getLinks(Long chatId) {
        return restClient.get()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    public DTO.LinkResponse addLink(Long chatId, DTO.AddLinkRequest linkRequest) {
        return restClient.post()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .body(linkRequest)
            .retrieve()
            .body(DTO.LinkResponse.class);
    }

    public DTO.LinkResponse deleteLink(Long chatId, DTO.RemoveLinkRequest linkRequest) {
        return restClient .method(HttpMethod.DELETE) // Используем метод DELETE явно
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .body(linkRequest) // Используем bodyValue() вместо body()
            .retrieve()
            .body(DTO.LinkResponse.class);
    }
}

