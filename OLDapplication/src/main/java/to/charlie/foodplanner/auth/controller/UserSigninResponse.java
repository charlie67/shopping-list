package to.charlie.foodplanner.auth.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSigninResponse {
	private String username;
	private String token;
}
