package hr.abysalto.hiring.mid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteResponse {

    private Long id;
    private Long userId;
    private Long productId;
    private String message;
}

