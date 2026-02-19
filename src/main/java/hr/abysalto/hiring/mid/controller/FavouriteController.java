package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.controller.problemdetail.FavouriteAlreadyExistsProblemDetail;
import hr.abysalto.hiring.mid.controller.problemdetail.ProductNotFoundProblemDetail;
import hr.abysalto.hiring.mid.controller.specification.FavouriteV1;
import hr.abysalto.hiring.mid.dto.AddFavouriteRequest;
import hr.abysalto.hiring.mid.dto.FavouriteResponse;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.exception.FavouriteAlreadyExistsException;
import hr.abysalto.hiring.mid.exception.ProductNotFoundException;
import hr.abysalto.hiring.mid.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FavouriteController implements FavouriteV1 {

    private final FavouriteService favouriteService;

    @Override
    public FavouriteResponse addToFavourites(AddFavouriteRequest request) {
        log.info("Received request to add product {} to favourites", request.getProductId());
        return favouriteService.addToFavourites(request.getProductId());
    }

    @Override
    public List<ProductDto> getUserFavourites() {
        log.info("Received request to get user favourites");
        return favouriteService.getUserFavourites();
    }

    @Override
    public void removeFromFavourites(Long productId) {
        log.info("Received request to remove product {} from favourites", productId);
        favouriteService.removeFromFavourites(productId);
    }

    @ExceptionHandler
    public ProductNotFoundProblemDetail handleProductNotFoundException(ProductNotFoundException ex) {
        log.warn("Product not found, responding with product not found problem detail", ex);
        return new ProductNotFoundProblemDetail(ex.getMessage());
    }

    @ExceptionHandler
    public FavouriteAlreadyExistsProblemDetail handleFavouriteAlreadyExistsException(FavouriteAlreadyExistsException ex) {
        log.warn("Favourite already exists, responding with favourite already exists problem detail", ex);
        return new FavouriteAlreadyExistsProblemDetail(ex.getMessage());
    }
}

