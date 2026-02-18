package hr.abysalto.hiring.mid.controller.problemdetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public class UserAlreadyExistsProblemDetail extends ProblemDetail {

    public static final String TYPE = "https://api.example.com/problems/user-already-exists-problem-detail";
    public static final String TITLE = "User Already Exists Problem Detail";

    public UserAlreadyExistsProblemDetail(String detail) {
        super(HttpStatus.CONFLICT.value());
        setType(URI.create(TYPE));
        setTitle(TITLE);
        setDetail(detail);
        setProperty("timestamp", Instant.now());
    }

    public static UserAlreadyExistsProblemDetail forUsername(String username) {
        return new UserAlreadyExistsProblemDetail("Username '" + username + "' is already taken");
    }

    public static UserAlreadyExistsProblemDetail forEmail(String email) {
        return new UserAlreadyExistsProblemDetail("Email '" + email + "' is already registered");
    }
}

