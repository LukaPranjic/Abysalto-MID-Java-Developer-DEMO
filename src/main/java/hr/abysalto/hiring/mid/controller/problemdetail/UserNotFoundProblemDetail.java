package hr.abysalto.hiring.mid.controller.problemdetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public class UserNotFoundProblemDetail extends ProblemDetail {

    public static final String TYPE = "https://api.example.com/problems/user-not-found-problem-detail";
    public static final String TITLE = "User Not Found Problem Detail";

    public UserNotFoundProblemDetail(String detail) {
        super(HttpStatus.NOT_FOUND.value());
        setType(URI.create(TYPE));
        setTitle(TITLE);
        setDetail(detail);
        setProperty("timestamp", Instant.now());
    }
}

