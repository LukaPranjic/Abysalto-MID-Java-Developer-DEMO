package hr.abysalto.hiring.mid.controller.problemdetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public class TechnicalFailureProblemDetail extends ProblemDetail {

    public static final String TYPE = "https://api.example.com/problems/technical-failure-problem-detail";
    public static final String TITLE = "Technical Failure Problem Detail";

    public TechnicalFailureProblemDetail(String detail) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value());
        setType(URI.create(TYPE));
        setTitle(TITLE);
        setDetail(detail);
        setProperty("timestamp", Instant.now());
    }
}
