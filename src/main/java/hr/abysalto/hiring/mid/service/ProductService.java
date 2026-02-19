package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.client.ProductClient;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.dto.ProductsResponse;
import hr.abysalto.hiring.mid.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductClient productClient;

    public ProductsResponse getAllProducts() {
        log.info("Fetching all products from DummyJSON API");
        return productClient.getAllProducts();
    }

    public ProductsResponse getAllProducts(Integer limit, Integer skip) {
        log.info("Fetching products from DummyJSON API with limit: {} and skip: {}", limit, skip);
        return productClient.getAllProducts(limit, skip);
    }

    public ProductDto getProductById(Long productId) {
        log.info("Fetching product with id: {} from DummyJSON API", productId);
        try {
            return productClient.getProductById(productId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product with id: {} not found", productId);
            throw ProductNotFoundException.forId(productId);
        }
    }

    public boolean productExists(Long productId) {
        try {
            getProductById(productId);
            return true;
        } catch (ProductNotFoundException e) {
            return false;
        }
    }
}

