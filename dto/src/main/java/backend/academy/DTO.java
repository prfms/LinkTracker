package backend.academy;

import java.util.List;

public class DTO {
    public record User(Long id) {}

    public record LinkResponse(int id, String url, List<String> tags, List<String> filters) {}

    public record ApiErrorResponse(
            String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}

    public record AddLinkRequest(String link, List<String> tags, List<String> filters) {}

    public record ListLinksResponse(List<LinkResponse> links, Integer size) {}

    public record RemoveLinkRequest(String link) {}

    public record LinkUpdate(int id, String url, String description, List<Long> tgChatIds) {}
}
