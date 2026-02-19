package hr.abysalto.hiring.mid.util;

import hr.abysalto.hiring.mid.dto.FavouriteResponse;
import hr.abysalto.hiring.mid.repository.entity.Favourite;

public class FavouriteMapper {

    public static Favourite mapToFavourite(Long userId, Long productId) {
        return Favourite.builder()
                .userId(userId)
                .productId(productId)
                .build();
    }

    public static FavouriteResponse mapToResponse(Favourite favourite, String message) {
        return FavouriteResponse.builder()
                .id(favourite.getId())
                .userId(favourite.getUserId())
                .productId(favourite.getProductId())
                .message(message)
                .build();
    }
}
