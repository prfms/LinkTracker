package backend.academy.scrapper.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LinkDto {
    private int id;
    private String url;
    private Long chatId;
    private List<String> tags;
    private List<String> filters;
    private OffsetDateTime lastUpdated;
}
