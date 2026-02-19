package hr.abysalto.hiring.mid.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException forId(Long productId) {
        return new ProductNotFoundException("Product not found with id: " + productId);
    }
}

