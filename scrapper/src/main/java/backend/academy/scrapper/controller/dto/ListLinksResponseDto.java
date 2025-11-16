package backend.academy.scrapper.controller.dto;

import java.util.List;

public record ListLinksResponseDto(List<LinkResponseDto> links, int size) {}
