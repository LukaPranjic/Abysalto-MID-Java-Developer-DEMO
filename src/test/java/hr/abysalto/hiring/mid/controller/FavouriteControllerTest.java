package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.configuration.AbysaltoTestAbstract;
import hr.abysalto.hiring.mid.dto.AddFavouriteRequest;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.dto.RegisterRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavouriteControllerTest extends AbysaltoTestAbstract {

    private static final ProductDto SAMPLE_PRODUCT = ProductDto.builder()
            .id(1L)
            .title("iPhone 15")
            .description("Latest Apple smartphone")
            .category("smartphones")
            .price(999.99)
            .brand("Apple")
            .build();

    private static final RegisterRequest REGISTER_REQUEST = RegisterRequest.builder()
            .username("testuser")
            .email("testuser@example.com")
            .password("password123")
            .build();

    private Long testUserId;

    @BeforeEach
    @SneakyThrows
    protected void setUp() {
        super.setUp();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(REGISTER_REQUEST)))
                .andExpect(status().isCreated());

        testUserId = userRepository.findByUsername("testuser")
                .orElseThrow(() -> new RuntimeException("Test user not found"))
                .getId();
    }

    @Nested
    @DisplayName("POST /api/favourites")
    class AddToFavouritesTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should add product to favourites successfully")
        void shouldAddProductToFavouritesSuccessfully() {
            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.userId").value(testUserId))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.message").value("Product added to favourites successfully"));

            verify(productClient, times(1)).getProductById(1L);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 409 when product already in favourites (idempotency)")
        void shouldReturn409WhenProductAlreadyInFavourites() {
            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/favourite-already-exists-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Favourite Already Exists Problem Detail"))
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 404 when product does not exist")
        void shouldReturn404WhenProductNotFound() {
            when(productClient.getProductById(999L))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            HttpHeaders.EMPTY,
                            null,
                            null));

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(999L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/product-not-found-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Product Not Found Problem Detail"))
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when productId is null")
        void shouldReturn400WhenProductIdIsNull() {
            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(null)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400WhenRequestBodyIsEmpty() {
            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should allow different users to favourite same product")
        void shouldAllowDifferentUsersToFavouriteSameProduct() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("anotheruser")
                    .email("anotheruser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser("anotheruser")))
                    .andExpect(status().isCreated());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when external service is unavailable")
        void shouldReturn500WhenExternalServiceUnavailable() {
            when(productClient.getProductById(anyLong()))
                    .thenThrow(new ResourceAccessException("Connection refused"));

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/favourites")
    class GetUserFavouritesTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            mockMvc.perform(get("/api/favourites"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return empty list when no favourites")
        void shouldReturnEmptyListWhenNoFavourites() {
            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return user favourites successfully")
        void shouldReturnUserFavouritesSuccessfully() {
            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("iPhone 15"))
                    .andExpect(jsonPath("$[0].price").value(999.99));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return multiple favourites")
        void shouldReturnMultipleFavourites() {
            ProductDto secondProduct = ProductDto.builder()
                    .id(2L)
                    .title("Samsung Galaxy S24")
                    .price(899.99)
                    .build();

            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);
            when(productClient.getProductById(2L)).thenReturn(secondProduct);

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(2L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return same favourites for repeated calls (idempotency)")
        void shouldReturnSameFavouritesForRepeatedCalls() {
            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(get("/api/favourites")
                                .with(authenticatedUser()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andExpect(jsonPath("$[0].id").value(1));
            }
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return only current user's favourites")
        void shouldReturnOnlyCurrentUserFavourites() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("otheruser")
                    .email("otheruser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            ProductDto secondProduct = ProductDto.builder()
                    .id(2L)
                    .title("Samsung Galaxy S24")
                    .price(899.99)
                    .build();

            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);
            when(productClient.getProductById(2L)).thenReturn(secondProduct);

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(2L).build()))
                            .with(authenticatedUser("otheruser")))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1));

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser("otheruser")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(2));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should handle deleted products gracefully")
        void shouldHandleDeletedProductsGracefully() {
            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            reset(productClient);
            when(productClient.getProductById(1L))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            HttpHeaders.EMPTY,
                            null,
                            null));

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/favourites/{productId}")
    class RemoveFromFavouritesTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            mockMvc.perform(delete("/api/favourites/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should remove favourite successfully")
        void shouldRemoveFavouriteSuccessfully() {
            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(delete("/api/favourites/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should be idempotent - removing non-existent favourite returns 204")
        void shouldBeIdempotentRemovingNonExistentFavourite() {
            mockMvc.perform(delete("/api/favourites/999")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete("/api/favourites/999")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should not affect other users favourites")
        void shouldNotAffectOtherUsersFavourites() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("thirduser")
                    .email("thirduser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser("thirduser")))
                    .andExpect(status().isCreated());

            mockMvc.perform(delete("/api/favourites/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser("thirduser")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should allow re-adding after removal")
        void shouldAllowReAddingAfterRemoval() {
            when(productClient.getProductById(1L)).thenReturn(SAMPLE_PRODUCT);

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(delete("/api/favourites/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(post("/api/favourites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddFavouriteRequest.builder().productId(1L).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/favourites")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }
}
