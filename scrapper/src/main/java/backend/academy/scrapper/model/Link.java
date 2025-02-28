package backend.academy.scrapper.model;
import java.util.List;

public record Link(int id, String url, Long chatId, List<String> tags, List<String> filters) {
}
