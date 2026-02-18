package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.dto.LoginRequest;
import hr.abysalto.hiring.mid.dto.RegisterRequest;
import hr.abysalto.hiring.mid.entity.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth Controller - Database Unavailable Tests")
class AuthControllerDbUnavailableTest extends AbstractControllerTest {

    @Nested
    @DisplayName("POST /api/auth/register - DB Unavailable")
    class RegisterDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database is unavailable during registration")
        void shouldReturn500WhenDbUnavailableDuringRegistration() {
            doThrow(new DataAccessResourceFailureException("Database connection failed"))
                    .when(userRepository).findByUsername(anyString());

            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("testuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database fails during user save")
        void shouldReturn500WhenDbFailsDuringSave() {
            doThrow(new DataAccessResourceFailureException("Database write failed"))
                    .when(userRepository).save(any(User.class));

            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("testuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database connection times out")
        void shouldReturn500WhenDbConnectionTimesOut() {
            doThrow(new DataAccessResourceFailureException("Connection timed out"))
                    .when(userRepository).findByUsername(anyString());

            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("testuser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.detail", containsString("Connection timed out")));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - DB Unavailable")
    class LoginDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database is unavailable during login")
        void shouldReturn500WhenDbUnavailableDuringLogin() {
            doThrow(new DataAccessResourceFailureException("Database connection failed"))
                    .when(userRepository).findByUsername(anyString());

            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database connection is lost during authentication")
        void shouldReturn500WhenDbConnectionLostDuringAuth() {
            doThrow(new DataAccessResourceFailureException("Connection lost"))
                    .when(userRepository).findByUsername(anyString());

            LoginRequest request = LoginRequest.builder()
                    .username("existinguser")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.detail", containsString("Connection lost")));
        }
    }
}
