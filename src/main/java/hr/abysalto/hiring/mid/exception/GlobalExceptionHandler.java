package hr.abysalto.hiring.mid.exception;

import hr.abysalto.hiring.mid.controller.problemdetail.TechnicalFailureProblemDetail;
import hr.abysalto.hiring.mid.controller.problemdetail.ValidationFailureProblemDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ValidationFailureProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Method argument is not valid, responding with validation failure problem detail", ex);
        List<ValidationFailureProblemDetail.Violation> violations = ex.getFieldErrors().stream().map(fe ->
            new ValidationFailureProblemDetail.Violation(
                    fe.getField(),
                    fe.getRejectedValue() == null ? null : fe.getRejectedValue().toString(),
                    fe.getDefaultMessage()
            )
        ).toList();
        return new ValidationFailureProblemDetail(violations);
    }

    @ExceptionHandler
    public TechnicalFailureProblemDetail handleGenericException(Exception ex) {
        log.error("An unexpected exception has occurred, responding with technical failure problem detail", ex);
        return new TechnicalFailureProblemDetail(ex.getMessage());
    }
}

