package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.configuration.AbysaltoTestAbstract;
import hr.abysalto.hiring.mid.dto.AddToCartRequest;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.dto.RegisterRequest;
import hr.abysalto.hiring.mid.repository.entity.CartItem;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerDbUnavailableTest extends AbysaltoTestAbstract {

    private ProductDto sampleProduct;

    @BeforeEach
    @SneakyThrows
    protected void setUp() {
        super.setUp();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        sampleProduct = ProductDto.builder()
                .id(1L)
                .title("iPhone 15")
                .description("Latest Apple smartphone")
                .category("smartphones")
                .price(999.99)
                .brand("Apple")
                .build();
    }

    @Nested
    @DisplayName("POST /api/cart - DB Unavailable")
    class AddToCartDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database is unavailable during add to cart check")
        void shouldReturn500WhenDbUnavailableDuringAddToCartCheck() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            doThrow(new DataAccessResourceFailureException("Database connection failed"))
                    .when(cartItemRepository).existsByUserIdAndProductId(anyLong(), anyLong());

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database fails during cart item save")
        void shouldReturn500WhenDbFailsDuringCartItemSave() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            doThrow(new DataAccessResourceFailureException("Database write failed"))
                    .when(cartItemRepository).save(any(CartItem.class));

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }
    }

    @Nested
    @DisplayName("GET /api/cart - DB Unavailable")
    class GetUserCartDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database is unavailable during get cart")
        void shouldReturn500WhenDbUnavailableDuringGetCart() {
            doThrow(new DataAccessResourceFailureException("Database connection failed"))
                    .when(cartItemRepository).findByUserId(anyLong());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }
    }

    @Nested
    @DisplayName("DELETE /api/cart/{productId} - DB Unavailable")
    class RemoveFromCartDbUnavailableTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when database is unavailable during remove from cart")
        void shouldReturn500WhenDbUnavailableDuringRemoveFromCart() {
            doThrow(new DataAccessResourceFailureException("Database connection failed"))
                    .when(cartItemRepository).deleteByUserIdAndProductId(anyLong(), anyLong());

            mockMvc.perform(delete("/api/cart/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }
    }
}

