package backend.academy;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class DTO {
    public record User(@Min(1) long id) {}

    public record LinkResponse(int id, String url, List<String> tags, List<String> filters) {}

    public record ApiErrorResponse(
            String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}

    public record AddLinkRequest(
            @NotNull @NotBlank(message = "Ссылка не может быть пустой") String link,
            List<String> tags,
            List<String> filters) {}

    public record ListLinksResponse(List<LinkResponse> links, Integer size) {}

    public record RemoveLinkRequest(@NotNull @NotBlank(message = "Ссылка не может быть пустой") String link) {}

    public record LinkUpdate(int id, String url, String description, List<Long> tgChatIds) {}
}
