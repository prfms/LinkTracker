package backend.academy.bot.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LinkResponseDto {
    private int id;
    private String url;
    private List<String> tags;
    private List<String> filters;
}
