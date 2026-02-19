package hr.abysalto.hiring.mid.exception;

public class CartItemAlreadyExistsException extends RuntimeException {

    public CartItemAlreadyExistsException(String message) {
        super(message);
    }

    public static CartItemAlreadyExistsException forProduct(Long productId) {
        return new CartItemAlreadyExistsException("Product with id " + productId + " is already in cart");
    }
}

