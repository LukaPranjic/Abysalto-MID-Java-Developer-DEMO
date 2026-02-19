package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.configuration.AbysaltoTestAbstract;
import hr.abysalto.hiring.mid.dto.AddFavouriteRequest;
import hr.abysalto.hiring.mid.dto.RegisterRequest;
import hr.abysalto.hiring.mid.repository.entity.Favourite;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FavouriteControllerDbUnavailableTest extends AbysaltoTestAbstract {

    private static final RegisterRequest REGISTER_REQUEST = RegisterRequest.builder()
            .username("testuser")
            .email("testuser@example.com")
            .password("password123")
            .build();

    @BeforeEach
    @SneakyThrows
    protected void setUp() {
        super.setUp();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(REGISTER_REQUEST)))
                .andExpect(status().isCreated());
    }

    @Nested
    @DisplayName("POST /api/favourites")
    class AddToFavouritesDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when favourites database is unavailable during saving")
        void shouldReturn500WhenFavouritesDatabaseIsUnavailableDuringSaving() {
            doThrow(new ResourceAccessException("Database unavailable"))
                    .when(favouriteRepository).save(any(Favourite.class));

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.detail").value("Database unavailable"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when favourites database is unavailable during check if it's already favourite")
        void shouldReturn500WhenFavouritesDatabaseIsUnavailableDuringCheckIfItsAlreadyFavourite() {
            doThrow(new ResourceAccessException("Database unavailable"))
                    .when(favouriteRepository).existsByUserIdAndProductId(anyLong(), anyLong());

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.detail").value("Database unavailable"))
                    .andExpect(jsonPath("$.status").value(500));
        }
    }

    @Nested
    @DisplayName("GET /api/favourites")
    class GetFavouritesDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when favourites database is unavailable during retrieving favourites")
        void shouldReturn500WhenFavouritesDatabaseIsUnavailableDuringRetrievingFavourites() {
            doThrow(new ResourceAccessException("Database unavailable"))
                    .when(favouriteRepository).findByUserId(anyLong());

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(get("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.detail").value("Database unavailable"))
                    .andExpect(jsonPath("$.status").value(500));
        }
    }

    @Nested
    @DisplayName("DELETE /api/favourites/{productId}")
    class DeleteFavouriteDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when favourites database is unavailable during deleting")
        void shouldReturn500WhenFavouritesDatabaseIsUnavailableDuringDeleting() {
            doThrow(new ResourceAccessException("Database unavailable"))
                    .when(favouriteRepository).deleteByUserIdAndProductId(anyLong(), anyLong());

            mockMvc.perform(delete("/api/favourites/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.detail").value("Database unavailable"))
                    .andExpect(jsonPath("$.status").value(500));
        }
    }
}
