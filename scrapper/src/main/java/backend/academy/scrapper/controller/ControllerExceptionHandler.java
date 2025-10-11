package backend.academy.scrapper.controller;

import backend.academy.scrapper.controller.dto.ApiErrorResponseDto;
import backend.academy.scrapper.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiErrorResponseDto handleUserNotFoundException(NotFoundException exception) {
        return new ApiErrorResponseDto(
                "Сущность не найдена",
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                getListStringStackTrace(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponseDto handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new ApiErrorResponseDto(
                "Некорректные параметры запроса", "400", "BadRequestException", "Ошибка валидации данных", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponseDto handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return new ApiErrorResponseDto(
                "Некорректные параметры запроса", "400", "BadRequestException", "Ошибка валидации параметров", errors);
    }

    /*@ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponseDto handleGenericException(Exception ex) {
        return new ApiErrorResponseDto(
                "Внутренняя ошибка сервера", "500", "InternalServerException", ex.getMessage(), List.of());
    }*/

    private List<String> getListStringStackTrace(Exception exception) {
        return Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
    }
}
