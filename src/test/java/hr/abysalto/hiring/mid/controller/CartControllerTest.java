package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.configuration.AbysaltoTestAbstract;
import hr.abysalto.hiring.mid.dto.AddToCartRequest;
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

class CartControllerTest extends AbysaltoTestAbstract {

    private ProductDto sampleProduct;
    private Long testUserId;

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

        testUserId = userRepository.findByUsername("testuser")
                .orElseThrow(() -> new RuntimeException("Test user not found"))
                .getId();

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
    @DisplayName("POST /api/cart")
    class AddToCartTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should add product to cart successfully")
        void shouldAddProductToCartSuccessfully() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.userId").value(testUserId))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.quantity").value(2))
                    .andExpect(jsonPath("$.message").value("Product added to cart successfully"));

            verify(productClient, times(1)).getProductById(1L);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should add product with default quantity of 1")
        void shouldAddProductWithDefaultQuantity() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.quantity").value(1));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 409 when product already in cart")
        void shouldReturn409WhenProductAlreadyInCart() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/cart-item-already-exists-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Cart Item Already Exists Problem Detail"))
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

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(999L)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
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
            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(null)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400WhenRequestBodyIsEmpty() {
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 400 when quantity is zero or negative")
        void shouldReturn400WhenQuantityIsInvalid() {
            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(0)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest());

            AddToCartRequest negativeRequest = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(-1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(negativeRequest))
                            .with(authenticatedUser()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should allow different users to add same product to cart")
        void shouldAllowDifferentUsersToAddSameProductToCart() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("anotheruser")
                    .email("anotheruser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/cart")
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

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/cart")
    class GetUserCartTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            mockMvc.perform(get("/api/cart"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return empty cart when no items")
        void shouldReturnEmptyCartWhenNoItems() {
            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(0)))
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.totalPrice").value(0.0));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return user cart successfully")
        void shouldReturnUserCartSuccessfully() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            AddToCartRequest request = AddToCartRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build();

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].product.id").value(1))
                    .andExpect(jsonPath("$.items[0].product.title").value("iPhone 15"))
                    .andExpect(jsonPath("$.items[0].product.price").value(999.99))
                    .andExpect(jsonPath("$.items[0].quantity").value(2))
                    .andExpect(jsonPath("$.totalItems").value(2))
                    .andExpect(jsonPath("$.totalPrice").value(1999.98));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return multiple cart items with correct totals")
        void shouldReturnMultipleCartItemsWithCorrectTotals() {
            ProductDto secondProduct = ProductDto.builder()
                    .id(2L)
                    .title("Samsung Galaxy S24")
                    .price(899.99)
                    .build();

            when(productClient.getProductById(1L)).thenReturn(sampleProduct);
            when(productClient.getProductById(2L)).thenReturn(secondProduct);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(2).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(2L).quantity(3).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.totalItems").value(5))
                    .andExpect(jsonPath("$.totalPrice").value(closeTo(4699.95, 0.01)));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return same cart for repeated calls (idempotency)")
        void shouldReturnSameCartForRepeatedCalls() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(1).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(get("/api/cart")
                                .with(authenticatedUser()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items", hasSize(1)))
                        .andExpect(jsonPath("$.items[0].product.id").value(1));
            }
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return only current user's cart items")
        void shouldReturnOnlyCurrentUserCartItems() {
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

            when(productClient.getProductById(1L)).thenReturn(sampleProduct);
            when(productClient.getProductById(2L)).thenReturn(secondProduct);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(1).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(2L).quantity(1).build()))
                            .with(authenticatedUser("otheruser")))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].product.id").value(1));

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser("otheruser")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].product.id").value(2));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should handle deleted products gracefully")
        void shouldHandleDeletedProductsGracefully() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(1).build()))
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

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(0)))
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.totalPrice").value(0.0));
        }
    }

    @Nested
    @DisplayName("DELETE /api/cart/{productId}")
    class RemoveFromCartTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            mockMvc.perform(delete("/api/cart/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should remove item from cart successfully")
        void shouldRemoveItemFromCartSuccessfully() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(1).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)));

            mockMvc.perform(delete("/api/cart/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should be idempotent - removing non-existent item returns 204")
        void shouldBeIdempotentRemovingNonExistentItem() {
            mockMvc.perform(delete("/api/cart/999")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete("/api/cart/999")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @SneakyThrows
        @DisplayName("Should not affect other users cart items")
        void shouldNotAffectOtherUsersCartItems() {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("thirduser")
                    .email("thirduser@example.com")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(1).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(1).build()))
                            .with(authenticatedUser("thirduser")))
                    .andExpect(status().isCreated());

            mockMvc.perform(delete("/api/cart/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)));

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser("thirduser")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].product.id").value(1));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should allow re-adding after removal")
        void shouldAllowReAddingAfterRemoval() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(1).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(delete("/api/cart/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(AddToCartRequest.builder().productId(1L).quantity(3).build()))
                            .with(authenticatedUser()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/cart")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].quantity").value(3));
        }
    }
}

