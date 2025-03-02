package backend.academy.scrapper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class Link {
    private int id;
    private String url;
    private Long chatId;
    private List<String> tags;
    private List<String> filters;
    private Instant lastUpdated;
}
