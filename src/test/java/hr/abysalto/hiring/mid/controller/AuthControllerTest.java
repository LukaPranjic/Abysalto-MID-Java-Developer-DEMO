package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.dto.LoginRequest;
import hr.abysalto.hiring.mid.dto.RegisterRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends AbstractControllerTest {

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @SneakyThrows
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("testuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("testuser@example.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_USER"))
                    .andExpect(jsonPath("$.message").value("User registered successfully"));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 409 when username already exists (idempotency)")
        void shouldReturn409WhenUsernameExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("existinguser")
                    .email("first@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            RegisterRequest duplicateRequest = RegisterRequest.builder()
                    .username("existinguser")
                    .email("different@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/user-already-exists-problem-detail"))
                    .andExpect(jsonPath("$.title").value("User Already Exists Problem Detail"))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.detail", containsString("existinguser")));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 409 when email already exists (idempotency)")
        void shouldReturn409WhenEmailExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("firstuser")
                    .email("duplicate@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            RegisterRequest duplicateRequest = RegisterRequest.builder()
                    .username("differentuser")
                    .email("duplicate@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/user-already-exists-problem-detail"))
                    .andExpect(jsonPath("$.title").value("User Already Exists Problem Detail"))
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when username is blank (validation)")
        void shouldReturn400WhenUsernameIsBlank() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("")
                    .email("test@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when username is null (validation)")
        void shouldReturn400WhenUsernameIsNull() {
            RegisterRequest request = RegisterRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when email is blank (validation)")
        void shouldReturn400WhenEmailIsBlank() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when password is blank (validation)")
        void shouldReturn400WhenPasswordIsBlank() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .password("")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when request body is empty (validation)")
        void shouldReturn400WhenRequestBodyIsEmpty() {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @BeforeEach
        @SneakyThrows
        void registerUser() {
            RegisterRequest request = RegisterRequest.builder()
                    .username("loginuser")
                    .email("loginuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            LoginRequest request = LoginRequest.builder()
                    .username("loginuser")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.username").value("loginuser"))
                    .andExpect(jsonPath("$.email").value("loginuser@example.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_USER"))
                    .andExpect(jsonPath("$.message").value("Login successful"));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should login multiple times successfully (idempotency)")
        void shouldLoginMultipleTimesSuccessfully() {
            LoginRequest request = LoginRequest.builder()
                    .username("loginuser")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("loginuser"));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when password is wrong")
        void shouldReturn401WhenPasswordIsWrong() {
            LoginRequest request = LoginRequest.builder()
                    .username("loginuser")
                    .password("wrongpassword")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/authentication-failed-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Authentication Failed Problem Detail"))
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when user does not exist")
        void shouldReturn401WhenUserDoesNotExist() {
            LoginRequest request = LoginRequest.builder()
                    .username("nonexistentuser")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when username is blank (validation)")
        void shouldReturn400WhenUsernameIsBlank() {
            LoginRequest request = LoginRequest.builder()
                    .username("")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when password is blank (validation)")
        void shouldReturn400WhenPasswordIsBlank() {
            LoginRequest request = LoginRequest.builder()
                    .username("loginuser")
                    .password("")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when request body is empty (validation)")
        void shouldReturn400WhenRequestBodyIsEmpty() {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return current user when authenticated via session")
        void shouldReturnCurrentUserWhenAuthenticated() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("sessionuser")
                    .email("sessionuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = LoginRequest.builder()
                    .username("sessionuser")
                    .password("password123")
                    .build();

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            mockMvc.perform(get("/api/auth/me")
                            .session((org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("sessionuser"))
                    .andExpect(jsonPath("$.email").value("sessionuser@example.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_USER"))
                    .andExpect(jsonPath("$.message").value("User retrieved successfully"));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return same user on multiple calls (idempotency)")
        void shouldReturnSameUserOnMultipleCalls() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("idempotentuser")
                    .email("idempotentuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = LoginRequest.builder()
                    .username("idempotentuser")
                    .password("password123")
                    .build();

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            org.springframework.mock.web.MockHttpSession session =
                    (org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession();

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(get("/api/auth/me")
                                .session(session))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value("idempotentuser"));
            }
        }
    }
}
