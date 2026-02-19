package hr.abysalto.hiring.mid.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("CART_ITEMS")
public class CartItem {

    @Id
    private Long id;

    private Long userId;

    private Long productId;

    private Integer quantity;
}

