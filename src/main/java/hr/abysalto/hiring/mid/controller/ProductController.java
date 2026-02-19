package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.controller.problemdetail.ProductNotFoundProblemDetail;
import hr.abysalto.hiring.mid.controller.specification.ProductV1;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.dto.ProductsResponse;
import hr.abysalto.hiring.mid.exception.ProductNotFoundException;
import hr.abysalto.hiring.mid.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductController implements ProductV1 {

    private final ProductService productService;

    @Override
    public ProductsResponse getAllProducts(Integer limit, Integer skip) {
        log.info("Received request to get all products with limit: {} and skip: {}", limit, skip);
        return productService.getAllProducts(limit, skip);
    }

    @Override
    public ProductDto getProductById(Long productId) {
        log.info("Received request to get product with id: {}", productId);
        return productService.getProductById(productId);
    }

    @ExceptionHandler
    public ProductNotFoundProblemDetail handleProductNotFoundException(ProductNotFoundException ex) {
        log.warn("Product not found, responding with product not found problem detail", ex);
        return new ProductNotFoundProblemDetail(ex.getMessage());
    }
}

