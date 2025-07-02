package backend.academy.scrapper.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiErrorResponseDto {
    private String description;
    private String code;
    private String exceptionName;
    private String exceptionMessage;
    private List<String> stackTrace;
}
