package to.charlie.foodplanner.auth.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSigninRequest {
    private String username;
    
    private String password;
}
