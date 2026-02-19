package hr.abysalto.hiring.mid.controller.problemdetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public class ProductNotFoundProblemDetail extends ProblemDetail {

    public static final String TYPE = "https://api.example.com/problems/product-not-found-problem-detail";
    public static final String TITLE = "Product Not Found Problem Detail";

    public ProductNotFoundProblemDetail(String detail) {
        super(HttpStatus.NOT_FOUND.value());
        setType(URI.create(TYPE));
        setTitle(TITLE);
        setDetail(detail);
        setProperty("timestamp", Instant.now());
    }
}

