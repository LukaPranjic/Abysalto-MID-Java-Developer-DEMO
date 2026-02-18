package hr.abysalto.hiring.mid.entity;

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
@Table("USERS")
public class User {

    @Id
    private Long id;

    private String username;

    private String email;

    private String password;

    private String role;

    private boolean enabled;
}

