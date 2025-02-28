package backend.academy;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

public class DTO {
    @Getter
    @Setter
    public record User(
        Long id
    ) {}

    @Getter
    @Setter
    public record LinkResponse(
        int id,
        String url,
        List<String> tags,
        List<String> filters
    ) {}

    @Getter
    @Setter
    public record ApiErrorResponse(
        String description,
        String code,
        String exceptionName,
        String exceptionMessage,
        List<String> stacktrace
    ) {}

    @Getter
    @Setter
    public record AddLinkRequest(
        String link,
        List<String> tags,
        List<String> filters
    ) {}

    @Getter
    @Setter
    public record ListLinksResponse(
        List<LinkResponse> links,
        Integer size
    ) {}

    @Getter
    @Setter
    public record RemoveLinkRequest(
        String link
    ) {}
}
