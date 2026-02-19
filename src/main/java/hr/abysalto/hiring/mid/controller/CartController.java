package hr.abysalto.hiring.mid.controller;

import hr.abysalto.hiring.mid.controller.problemdetail.CartItemAlreadyExistsProblemDetail;
import hr.abysalto.hiring.mid.controller.problemdetail.ProductNotFoundProblemDetail;
import hr.abysalto.hiring.mid.controller.specification.CartV1;
import hr.abysalto.hiring.mid.dto.AddToCartRequest;
import hr.abysalto.hiring.mid.dto.CartItemResponse;
import hr.abysalto.hiring.mid.dto.CartResponse;
import hr.abysalto.hiring.mid.exception.CartItemAlreadyExistsException;
import hr.abysalto.hiring.mid.exception.ProductNotFoundException;
import hr.abysalto.hiring.mid.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CartController implements CartV1 {

    private final CartService cartService;

    @Override
    public CartItemResponse addToCart(AddToCartRequest request) {
        log.info("Received request to add product {} to cart with quantity {}",
                request.getProductId(), request.getQuantity());
        return cartService.addToCart(request.getProductId(), request.getQuantity());
    }

    @Override
    public CartResponse getUserCart() {
        log.info("Received request to get user cart");
        return cartService.getUserCart();
    }

    @Override
    public void removeFromCart(Long productId) {
        log.info("Received request to remove product {} from cart", productId);
        cartService.removeFromCart(productId);
    }

    @ExceptionHandler
    public ProductNotFoundProblemDetail handleProductNotFoundException(ProductNotFoundException ex) {
        log.warn("Product not found, responding with product not found problem detail", ex);
        return new ProductNotFoundProblemDetail(ex.getMessage());
    }

    @ExceptionHandler
    public CartItemAlreadyExistsProblemDetail handleCartItemAlreadyExistsException(CartItemAlreadyExistsException ex) {
        log.warn("Cart item already exists, responding with cart item already exists problem detail", ex);
        return new CartItemAlreadyExistsProblemDetail(ex.getMessage());
    }
}

