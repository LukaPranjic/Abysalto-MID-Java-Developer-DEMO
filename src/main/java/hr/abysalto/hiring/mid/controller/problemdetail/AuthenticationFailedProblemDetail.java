package hr.abysalto.hiring.mid.controller.problemdetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public class AuthenticationFailedProblemDetail extends ProblemDetail {

    public static final String TYPE = "https://api.example.com/problems/authentication-failed-problem-detail";
    public static final String TITLE = "Authentication Failed Problem Detail";

    public AuthenticationFailedProblemDetail(String detail) {
        super(HttpStatus.UNAUTHORIZED.value());
        setType(URI.create(TYPE));
        setTitle(TITLE);
        setDetail(detail);
        setProperty("timestamp", Instant.now());
    }

    public static AuthenticationFailedProblemDetail invalidCredentials() {
        return new AuthenticationFailedProblemDetail("Invalid username or password");
    }

    public static AuthenticationFailedProblemDetail notAuthenticated() {
        return new AuthenticationFailedProblemDetail("You must be authenticated to access this resource");
    }
}

