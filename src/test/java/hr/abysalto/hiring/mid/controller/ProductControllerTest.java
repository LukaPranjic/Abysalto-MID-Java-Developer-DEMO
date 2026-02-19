package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.configuration.AbysaltoTestAbstract;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.dto.ProductsResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest extends AbysaltoTestAbstract {

    private ProductDto sampleProduct;
    private ProductsResponse sampleProductsResponse;

    @BeforeEach
    protected void setUp() {
        sampleProduct = ProductDto.builder()
                .id(1L)
                .title("iPhone 15")
                .description("Latest Apple smartphone")
                .category("smartphones")
                .price(999.99)
                .discountPercentage(5.0)
                .rating(4.5)
                .stock(100)
                .brand("Apple")
                .sku("IPHONE15-128")
                .thumbnail("https://example.com/iphone15.jpg")
                .build();

        sampleProductsResponse = ProductsResponse.builder()
                .products(List.of(sampleProduct))
                .total(1)
                .skip(0)
                .limit(30)
                .build();
    }

    @Nested
    @DisplayName("GET /api/products")
    class GetAllProductsTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(productClient);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return all products successfully when authenticated")
        void shouldReturnAllProductsSuccessfully() {
            when(productClient.getAllProducts(30, 0, null, null)).thenReturn(sampleProductsResponse);

            mockMvc.perform(get("/api/products")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products", hasSize(1)))
                    .andExpect(jsonPath("$.products[0].id").value(1))
                    .andExpect(jsonPath("$.products[0].title").value("iPhone 15"))
                    .andExpect(jsonPath("$.products[0].price").value(999.99))
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.skip").value(0))
                    .andExpect(jsonPath("$.limit").value(30));

            verify(productClient, times(1)).getAllProducts(30, 0, null, null);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return products with custom pagination parameters")
        void shouldReturnProductsWithCustomPagination() {
            ProductsResponse paginatedResponse = ProductsResponse.builder()
                    .products(List.of(sampleProduct))
                    .total(100)
                    .skip(10)
                    .limit(5)
                    .build();

            when(productClient.getAllProducts(5, 10, null, null)).thenReturn(paginatedResponse);

            mockMvc.perform(get("/api/products")
                            .param("limit", "5")
                            .param("skip", "10")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.skip").value(10))
                    .andExpect(jsonPath("$.limit").value(5))
                    .andExpect(jsonPath("$.total").value(100));

            verify(productClient, times(1)).getAllProducts(5, 10, null, null);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return empty products list when no products exist")
        void shouldReturnEmptyProductsList() {
            ProductsResponse emptyResponse = ProductsResponse.builder()
                    .products(List.of())
                    .total(0)
                    .skip(0)
                    .limit(30)
                    .build();

            when(productClient.getAllProducts(30, 0, null, null)).thenReturn(emptyResponse);

            mockMvc.perform(get("/api/products")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products", hasSize(0)))
                    .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return same results for repeated calls (idempotency)")
        void shouldReturnSameResultsForRepeatedCalls() {
            when(productClient.getAllProducts(30, 0, null, null)).thenReturn(sampleProductsResponse);

            mockMvc.perform(get("/api/products")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products[0].id").value(1));

            mockMvc.perform(get("/api/products")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products[0].id").value(1));

            mockMvc.perform(get("/api/products")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products[0].id").value(1));

            verify(productClient, times(3)).getAllProducts(30, 0, null, null);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when external service is unavailable")
        void shouldReturn500WhenExternalServiceUnavailable() {
            when(productClient.getAllProducts(anyInt(), anyInt(), any(), any()))
                    .thenThrow(new ResourceAccessException("Connection refused"));

            mockMvc.perform(get("/api/products")
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when external service times out")
        void shouldReturn500WhenExternalServiceTimesOut() {
            when(productClient.getAllProducts(anyInt(), anyInt(), any(), any()))
                    .thenThrow(new ResourceAccessException("Read timed out"));

            mockMvc.perform(get("/api/products")
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return products sorted by price ascending")
        void shouldReturnProductsSortedByPriceAscending() {
            when(productClient.getAllProducts(30, 0, "price", "asc")).thenReturn(sampleProductsResponse);

            mockMvc.perform(get("/api/products")
                            .param("sortBy", "price")
                            .param("order", "asc")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray());

            verify(productClient, times(1)).getAllProducts(30, 0, "price", "asc");
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return products sorted by rating descending")
        void shouldReturnProductsSortedByRatingDescending() {
            when(productClient.getAllProducts(30, 0, "rating", "desc")).thenReturn(sampleProductsResponse);

            mockMvc.perform(get("/api/products")
                            .param("sortBy", "rating")
                            .param("order", "desc")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray());

            verify(productClient, times(1)).getAllProducts(30, 0, "rating", "desc");
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return products with pagination and sorting combined")
        void shouldReturnProductsWithPaginationAndSorting() {
            ProductsResponse sortedPaginatedResponse = ProductsResponse.builder()
                    .products(List.of(sampleProduct))
                    .total(100)
                    .skip(20)
                    .limit(10)
                    .build();

            when(productClient.getAllProducts(10, 20, "title", "asc")).thenReturn(sortedPaginatedResponse);

            mockMvc.perform(get("/api/products")
                            .param("limit", "10")
                            .param("skip", "20")
                            .param("sortBy", "title")
                            .param("order", "asc")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.skip").value(20))
                    .andExpect(jsonPath("$.limit").value(10))
                    .andExpect(jsonPath("$.total").value(100));

            verify(productClient, times(1)).getAllProducts(10, 20, "title", "asc");
        }
    }

    @Nested
    @DisplayName("GET /api/products/{productId}")
    class GetProductByIdTests {

        @Test
        @SneakyThrows
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(productClient);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return product successfully when authenticated")
        void shouldReturnProductSuccessfully() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(get("/api/products/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("iPhone 15"))
                    .andExpect(jsonPath("$.description").value("Latest Apple smartphone"))
                    .andExpect(jsonPath("$.category").value("smartphones"))
                    .andExpect(jsonPath("$.price").value(999.99))
                    .andExpect(jsonPath("$.brand").value("Apple"));

            verify(productClient, times(1)).getProductById(1L);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() {
            when(productClient.getProductById(999L))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            HttpHeaders.EMPTY,
                            null,
                            null));

            mockMvc.perform(get("/api/products/999")
                            .with(authenticatedUser()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/product-not-found-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Product Not Found Problem Detail"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail", containsString("999")));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return same product for repeated calls (idempotency)")
        void shouldReturnSameProductForRepeatedCalls() {
            when(productClient.getProductById(1L)).thenReturn(sampleProduct);

            mockMvc.perform(get("/api/products/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("iPhone 15"));

            mockMvc.perform(get("/api/products/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("iPhone 15"));

            verify(productClient, times(2)).getProductById(1L);
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return different products for different IDs")
        void shouldReturnDifferentProductsForDifferentIds() {
            ProductDto anotherProduct = ProductDto.builder()
                    .id(2L)
                    .title("Samsung Galaxy S24")
                    .brand("Samsung")
                    .price(899.99)
                    .build();

            when(productClient.getProductById(1L)).thenReturn(sampleProduct);
            when(productClient.getProductById(2L)).thenReturn(anotherProduct);

            mockMvc.perform(get("/api/products/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("iPhone 15"));

            mockMvc.perform(get("/api/products/2")
                            .with(authenticatedUser()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.title").value("Samsung Galaxy S24"));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when external service is unavailable")
        void shouldReturn500WhenExternalServiceUnavailable() {
            when(productClient.getProductById(anyLong()))
                    .thenThrow(new ResourceAccessException("Connection refused"));

            mockMvc.perform(get("/api/products/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.type").value("https://api.example.com/problems/technical-failure-problem-detail"))
                    .andExpect(jsonPath("$.title").value("Technical Failure Problem Detail"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @SneakyThrows
        @DisplayName("Should return 500 when external service returns server error")
        void shouldReturn500WhenExternalServiceReturnsServerError() {
            when(productClient.getProductById(anyLong()))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Service Unavailable",
                            HttpHeaders.EMPTY,
                            null,
                            null));

            mockMvc.perform(get("/api/products/1")
                            .with(authenticatedUser()))
                    .andExpect(status().isInternalServerError());
        }
    }
}



