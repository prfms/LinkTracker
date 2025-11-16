package backend.academy.bot.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TrackingUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTrackingUrl {
    String message() default "Поддерживаются только ссылки на GitHub и Stack Overflow";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
