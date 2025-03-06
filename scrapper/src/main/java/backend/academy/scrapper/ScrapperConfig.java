package backend.academy.scrapper;

import backend.academy.scrapper.repository.InMemoryRepository;
import backend.academy.scrapper.repository.Repository;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    @NotEmpty String githubToken,
    StackOverflowCredentials stackOverflow,
    @NotEmpty String githubBaseUrl) {

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    @Bean
    public Repository linkRepository() {
        return new InMemoryRepository();
    }
}
