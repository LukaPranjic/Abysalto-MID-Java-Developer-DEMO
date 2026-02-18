package hr.abysalto.hiring.mid.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Field username cannot be blank")
    private String username;

    @NotBlank(message = "Field password cannot be blank")
    private String password;
}

