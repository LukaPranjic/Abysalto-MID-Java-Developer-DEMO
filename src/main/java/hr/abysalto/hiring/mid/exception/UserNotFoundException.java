package hr.abysalto.hiring.mid.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User with username '" + username + "' was not found");
    }

    public static UserNotFoundException forId(Long id) {
        return new UserNotFoundException("User with id '" + id + "' was not found");
    }
}

