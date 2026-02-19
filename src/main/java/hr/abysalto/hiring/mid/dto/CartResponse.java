package hr.abysalto.hiring.mid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private List<CartProductDto> items;
    private Integer totalItems;
    private Double totalPrice;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartProductDto {
        private ProductDto product;
        private Integer quantity;
    }
}

