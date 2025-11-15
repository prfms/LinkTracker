package backend.academy.scrapper.model;

import java.time.OffsetDateTime;
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
    private OffsetDateTime lastUpdated;
}
