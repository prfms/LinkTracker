package backend.academy.bot.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ListLinksResponseDto {
    private int size;
    private List<LinkResponseDto> links;
}
