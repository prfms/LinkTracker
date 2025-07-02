package backend.academy.bot.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class TrackingUrlValidator implements ConstraintValidator<ValidTrackingUrl, String> {
    private static final Pattern GITHUB_PATTERN = Pattern.compile("^https://github\\.com/[^/]+/[^/]+/?$");
    private static final Pattern STACKOVERFLOW_PATTERN =
            Pattern.compile("^https://stackoverflow\\.com/questions/\\d+/?.*$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return GITHUB_PATTERN.matcher(value).matches()
                || STACKOVERFLOW_PATTERN.matcher(value).matches();
    }
}
