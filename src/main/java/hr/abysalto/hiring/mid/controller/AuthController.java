package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.controller.problemdetail.UserNotFoundProblemDetail;
import hr.abysalto.hiring.mid.controller.specification.AuthV1;
import hr.abysalto.hiring.mid.dto.AuthResponse;
import hr.abysalto.hiring.mid.dto.LoginRequest;
import hr.abysalto.hiring.mid.dto.RegisterRequest;
import hr.abysalto.hiring.mid.exception.UserAlreadyExistsException;
import hr.abysalto.hiring.mid.exception.UserNotFoundException;
import hr.abysalto.hiring.mid.controller.problemdetail.AuthenticationFailedProblemDetail;
import hr.abysalto.hiring.mid.controller.problemdetail.UserAlreadyExistsProblemDetail;
import hr.abysalto.hiring.mid.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController implements AuthV1 {

    private final UserService userService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Received a request to register a new user with username: {}", request.getUsername());
        return userService.register(request);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("Received a request to login a new user with username: {}", request.getUsername());
        return userService.login(request, httpRequest, httpResponse);
    }

    @Override
    public AuthResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Received a request to get current user with username: {}", username);
        return userService.findByUsername(username)
                .map(user -> AuthResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .message("User retrieved successfully")
                        .build())
                .orElseThrow(() -> UserNotFoundException.forUsername(username));
    }

    @ExceptionHandler
    public UserAlreadyExistsProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("User already exists, responding with user already exists problem detail", ex);
        return new UserAlreadyExistsProblemDetail(ex.getMessage());
    }

    @ExceptionHandler
    public AuthenticationFailedProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials received, responding with authentication failed problem detail", ex);
        return AuthenticationFailedProblemDetail.invalidCredentials();
    }

    @ExceptionHandler
    public UserNotFoundProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found, responding with user not found problem detail", ex);
        return new UserNotFoundProblemDetail(ex.getMessage());
    }
}

