package backend.academy.scrapper.controller.dto;

import java.util.List;

public record LinkResponseDto
    (int id,
    String url,
    List<String> tags,
    List<String> filters)
{ }
