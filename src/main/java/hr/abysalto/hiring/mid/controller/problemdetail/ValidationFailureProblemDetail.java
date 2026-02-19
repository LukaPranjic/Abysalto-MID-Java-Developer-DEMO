package hr.abysalto.hiring.mid.controller.problemdetail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import java.net.URI;
import java.util.List;

@Getter
@Setter
public class ValidationFailureProblemDetail extends ProblemDetail {

    private static final String VALIDATION_FAILURE_PROBLEM_DETAIL_TITLE = "Validation Failure Problem Detail";
    private static final URI VALIDATION_FAILURE_PROBLEM_DETAIL_URI =
            URI.create("https://api.example.com/problems/validation-failure-problem-detail");
    private static final String DEFAULT_DETAIL =
            "Validation of the request failed. Check the violations for more information on the rejected property path, value, and for an expected value.";

    public record Violation(String propertyPath, String rejectedValue, String message) { }

    private List<Violation> violations;

    public ValidationFailureProblemDetail(String detail) {
        super(HttpStatus.BAD_REQUEST.value());
        this.setTitle(VALIDATION_FAILURE_PROBLEM_DETAIL_TITLE);
        this.setType(VALIDATION_FAILURE_PROBLEM_DETAIL_URI);
        this.setDetail(detail);
        this.violations = null;
    }

    public ValidationFailureProblemDetail(List<Violation> violations) {
        super(HttpStatus.BAD_REQUEST.value());
        this.setTitle(VALIDATION_FAILURE_PROBLEM_DETAIL_TITLE);
        this.setType(VALIDATION_FAILURE_PROBLEM_DETAIL_URI);
        this.setDetail(DEFAULT_DETAIL);
        this.violations = violations;
    }
}
