package hr.abysalto.hiring.mid.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFavouriteRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;
}

