package hr.abysalto.hiring.mid.controller.specification;

import hr.abysalto.hiring.mid.controller.problemdetail.CartItemAlreadyExistsProblemDetail;
import hr.abysalto.hiring.mid.controller.problemdetail.ProductNotFoundProblemDetail;
import hr.abysalto.hiring.mid.dto.AddToCartRequest;
import hr.abysalto.hiring.mid.dto.CartItemResponse;
import hr.abysalto.hiring.mid.dto.CartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "User shopping cart management endpoints")
@RequestMapping("/api/cart")
public interface CartV1 {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add product to cart",
            description = "Adds a product to the current user's shopping cart"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product added to cart successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CartItemResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProductNotFoundProblemDetail.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Product already in cart",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CartItemAlreadyExistsProblemDetail.class
                            )
                    )
            )
    })
    CartItemResponse addToCart(@RequestBody @Valid AddToCartRequest request);

    @GetMapping
    @Operation(
            summary = "Get user cart",
            description = "Retrieves all products in the current user's shopping cart"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart retrieved successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CartResponse.class
                            )
                    )
            )
    })
    CartResponse getUserCart();

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove product from cart",
            description = "Removes a product from the current user's shopping cart",
            parameters = {
                    @Parameter(
                            name = "productId",
                            description = "ID of the product to remove from cart",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product removed from cart successfully"
            )
    })
    void removeFromCart(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId
    );
}

