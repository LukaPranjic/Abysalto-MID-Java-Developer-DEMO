package hr.abysalto.hiring.mid.controller.problemdetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.URI;
import java.time.Instant;

@ResponseStatus(HttpStatus.CONFLICT)
public class CartItemAlreadyExistsProblemDetail extends ProblemDetail {

    public static final String TYPE = "https://api.example.com/problems/cart-item-already-exists-problem-detail";
    public static final String TITLE = "Cart Item Already Exists Problem Detail";

    public CartItemAlreadyExistsProblemDetail(String detail) {
        super(HttpStatus.CONFLICT.value());
        setType(URI.create(TYPE));
        setTitle(TITLE);
        setDetail(detail);
        setProperty("timestamp", Instant.now());
    }
}

