package backend.academy.scrapper.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LinkUpdateDto {
    private int id;
    private String url;
    private String description;
    private List<Long> tgChatIds;
}
