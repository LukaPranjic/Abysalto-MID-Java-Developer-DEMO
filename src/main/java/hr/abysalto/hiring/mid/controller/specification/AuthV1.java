package hr.abysalto.hiring.mid.controller.specification;

import hr.abysalto.hiring.mid.dto.AuthResponse;
import hr.abysalto.hiring.mid.dto.LoginRequest;
import hr.abysalto.hiring.mid.dto.RegisterRequest;
import hr.abysalto.hiring.mid.controller.problemdetail.AuthenticationFailedProblemDetail;
import hr.abysalto.hiring.mid.controller.problemdetail.UserAlreadyExistsProblemDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User authentication and registration endpoints")
@RequestMapping("/api/auth")
public interface AuthV1 {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided credentials"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserAlreadyExistsProblemDetail.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProblemDetail.class
                            )
                    )
            )
    })
    AuthResponse register(@RequestBody @Valid RegisterRequest request);

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticates user with username and password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthenticationFailedProblemDetail.class
                            )
                    )
            )
    })
    AuthResponse login(
            @RequestBody @Valid LoginRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse httpResponse
    );

    @GetMapping("/me")
    @Operation(summary = "Get current user",
            description = "Returns the currently authenticated user's information"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProblemDetail.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProblemDetail.class
                            )
                    )
            )
    })
    AuthResponse getCurrentUser();
}

