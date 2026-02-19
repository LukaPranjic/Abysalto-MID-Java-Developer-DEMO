package hr.abysalto.hiring.mid.util;

import hr.abysalto.hiring.mid.dto.CartItemResponse;
import hr.abysalto.hiring.mid.dto.CartResponse;
import hr.abysalto.hiring.mid.repository.entity.CartItem;

import java.util.List;

public class CartMapper {

    public static CartItem mapToCartItem(Long userId, Long productId, Integer quantity) {
        return CartItem.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity != null ? quantity : 1)
                .build();
    }

    public static CartItemResponse mapToResponse(CartItem cartItem, String message) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .userId(cartItem.getUserId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .message(message)
                .build();
    }

    public static CartResponse mapToResponse(List<CartResponse.CartProductDto> items, int totalItems, double totalPrice) {
        return CartResponse.builder()
                .items(items)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }
}
