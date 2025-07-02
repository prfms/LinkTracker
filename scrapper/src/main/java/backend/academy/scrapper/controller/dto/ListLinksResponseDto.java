package backend.academy.scrapper.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ListLinksResponseDto {
    private List<LinkResponseDto> links;
    private int size;
}
