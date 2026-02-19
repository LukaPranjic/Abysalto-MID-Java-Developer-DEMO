package hr.abysalto.hiring.mid.controller.specification;

import hr.abysalto.hiring.mid.controller.problemdetail.ProductNotFoundProblemDetail;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.dto.ProductsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "Product management endpoints")
@RequestMapping("/api/products")
public interface ProductV1 {

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Retrieves all products from the catalog with optional pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductsResponse.class)
                    )
            )
    })
    ProductsResponse getAllProducts(
            @Parameter(description = "Number of products to return")
            @RequestParam(required = false, defaultValue = "30") Integer limit,
            @Parameter(description = "Number of products to skip")
            @RequestParam(required = false, defaultValue = "0") Integer skip
    );

    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a single product by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            schema = @Schema(implementation = ProductNotFoundProblemDetail.class)
                    )
            )
    })
    ProductDto getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId
    );
}

