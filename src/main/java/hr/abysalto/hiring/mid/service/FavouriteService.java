package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.dto.FavouriteResponse;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.entity.Favourite;
import hr.abysalto.hiring.mid.entity.User;
import hr.abysalto.hiring.mid.exception.FavouriteAlreadyExistsException;
import hr.abysalto.hiring.mid.exception.ProductNotFoundException;
import hr.abysalto.hiring.mid.exception.UserNotFoundException;
import hr.abysalto.hiring.mid.repository.FavouriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public FavouriteResponse addToFavourites(Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Adding product {} to favourites for user {}", productId, username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.forUsername(username));

        if (!productService.productExists(productId)) {
            throw ProductNotFoundException.forId(productId);
        }

        if (favouriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw FavouriteAlreadyExistsException.forProduct(productId);
        }

        Favourite favourite = Favourite.builder()
                .userId(user.getId())
                .productId(productId)
                .build();

        Favourite savedFavourite = favouriteRepository.save(favourite);

        return FavouriteResponse.builder()
                .id(savedFavourite.getId())
                .userId(savedFavourite.getUserId())
                .productId(savedFavourite.getProductId())
                .message("Product added to favourites successfully")
                .build();
    }

    public List<ProductDto> getUserFavourites() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting favourites for user {}", username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.forUsername(username));

        List<Favourite> favourites = favouriteRepository.findByUserId(user.getId());

        return favourites.stream()
                .map(favourite -> {
                    try {
                        return productService.getProductById(favourite.getProductId());
                    } catch (ProductNotFoundException e) {
                        log.warn("Favourite product {} no longer exists", favourite.getProductId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public void removeFromFavourites(Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Removing product {} from favourites for user {}", productId, username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.forUsername(username));

        favouriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }
}

