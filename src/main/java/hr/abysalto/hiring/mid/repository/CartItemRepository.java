package hr.abysalto.hiring.mid.repository;

import hr.abysalto.hiring.mid.repository.entity.CartItem;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends CrudRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query("DELETE FROM CART_ITEMS WHERE USER_ID = :userId AND PRODUCT_ID = :productId")
    void deleteByUserIdAndProductId(Long userId, Long productId);
}

