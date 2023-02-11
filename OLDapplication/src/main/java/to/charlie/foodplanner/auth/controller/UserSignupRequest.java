package to.charlie.foodplanner.auth.controller;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignupRequest {
	@NotEmpty(message = "Username is required")
    private String username;
    
    @NotEmpty(message = "Password is required")
    @Size(min=8, message = "Password length should be 8 characters or more")
    private String password;
}
