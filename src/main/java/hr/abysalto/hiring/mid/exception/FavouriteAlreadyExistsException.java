package hr.abysalto.hiring.mid.exception;

public class FavouriteAlreadyExistsException extends RuntimeException {

    public FavouriteAlreadyExistsException(String message) {
        super(message);
    }

    public static FavouriteAlreadyExistsException forProduct(Long productId) {
        return new FavouriteAlreadyExistsException("Product with id " + productId + " is already in favourites");
    }
}

