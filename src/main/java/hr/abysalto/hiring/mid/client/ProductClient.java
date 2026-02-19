package hr.abysalto.hiring.mid.client;

import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.dto.ProductsResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/products")
public interface ProductClient {

    @GetExchange
    ProductsResponse getAllProducts();

    @GetExchange
    ProductsResponse getAllProducts(
            @RequestParam("limit") Integer limit,
            @RequestParam("skip") Integer skip
    );

    @GetExchange
    ProductsResponse getAllProducts(
            @RequestParam("limit") Integer limit,
            @RequestParam("skip") Integer skip,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order
    );

    @GetExchange("/{productId}")
    ProductDto getProductById(@PathVariable Long productId);
}

