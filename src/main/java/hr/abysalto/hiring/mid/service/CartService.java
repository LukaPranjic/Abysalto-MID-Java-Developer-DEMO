package hr.abysalto.hiring.mid.service;

import hr.abysalto.hiring.mid.dto.CartItemResponse;
import hr.abysalto.hiring.mid.dto.CartResponse;
import hr.abysalto.hiring.mid.dto.ProductDto;
import hr.abysalto.hiring.mid.repository.entity.CartItem;
import hr.abysalto.hiring.mid.repository.entity.User;
import hr.abysalto.hiring.mid.exception.CartItemAlreadyExistsException;
import hr.abysalto.hiring.mid.exception.ProductNotFoundException;
import hr.abysalto.hiring.mid.exception.UserNotFoundException;
import hr.abysalto.hiring.mid.repository.CartItemRepository;
import hr.abysalto.hiring.mid.util.CartMapper;
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
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public CartItemResponse addToCart(Long productId, Integer quantity) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Adding product {} to cart for user {} with quantity {}", productId, username, quantity);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.forUsername(username));

        if (!productService.productExists(productId)) {
            throw ProductNotFoundException.forId(productId);
        }

        if (cartItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw CartItemAlreadyExistsException.forProduct(productId);
        }

        CartItem cartItem = CartMapper.mapToCartItem(user.getId(), productId, quantity);

        CartItem savedCartItem = cartItemRepository.save(cartItem);

        return CartMapper.mapToResponse(savedCartItem, "Product added to cart successfully");
    }

    public CartResponse getUserCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting cart for user {}", username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.forUsername(username));

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());

        List<CartResponse.CartProductDto> items = cartItems.stream()
                .map(cartItem -> {
                    try {
                        ProductDto product = productService.getProductById(cartItem.getProductId());
                        return CartResponse.CartProductDto.builder()
                                .product(product)
                                .quantity(cartItem.getQuantity())
                                .build();
                    } catch (ProductNotFoundException e) {
                        log.warn("Cart product {} no longer exists", cartItem.getProductId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        int totalItems = items.stream()
                .mapToInt(CartResponse.CartProductDto::getQuantity)
                .sum();

        double totalPrice = items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        return CartMapper.mapToResponse(items, totalItems, totalPrice);
    }

    @Transactional
    public void removeFromCart(Long productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Removing product {} from cart for user {}", productId, username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.forUsername(username));

        cartItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }
}
