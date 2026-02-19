package hr.abysalto.hiring.mid.controller.specification;

import hr.abysalto.hiring.mid.controller.problemdetail.FavouriteAlreadyExistsProblemDetail;
import hr.abysalto.hiring.mid.controller.problemdetail.ProductNotFoundProblemDetail;
import hr.abysalto.hiring.mid.dto.AddFavouriteRequest;
import hr.abysalto.hiring.mid.dto.FavouriteResponse;
import hr.abysalto.hiring.mid.dto.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Favourites", description = "User favourite products management endpoints")
@RequestMapping("/api/favourites")
public interface FavouriteV1 {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add product to favourites",
            description = "Adds a product to the current user's favourites list"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product added to favourites successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = FavouriteResponse.class
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
                    description = "Product already in favourites",
                    content = @Content(
                            schema = @Schema(
                                    implementation = FavouriteAlreadyExistsProblemDetail.class
                            )
                    )
            )
    })
    FavouriteResponse addToFavourites(@RequestBody @Valid AddFavouriteRequest request);

    @GetMapping
    @Operation(
            summary = "Get user favourites",
            description = "Retrieves all products in the current user's favourites list"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Favourites retrieved successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = ProductDto.class
                                    )
                            )
                    )
            )
    })
    List<ProductDto> getUserFavourites();

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove product from favourites",
            description = "Removes a product from the current user's favourites list",
            parameters = {
                    @Parameter(
                            name = "productId",
                            description = "ID of the product to remove from favourites",
                            required = true
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product removed from favourites successfully"
            )
    })
    void removeFromFavourites(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId
    );
}

