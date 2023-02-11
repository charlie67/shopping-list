package to.charlie.foodplanner.auth.controller;

import com.backend.todolist.auth.jwt.JwtTokenGenerator;
import com.backend.todolist.auth.repository.UserRepository;
import com.backend.todolist.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class UserController {	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
    PasswordEncoder passwordEncoder;
    
	@Autowired
    JwtTokenGenerator jwtTokenGenerator;
    
	@Autowired
    UserRepository userRepository;
	
	@Autowired
	UserService userService;
    
	@ResponseStatus(code = HttpStatus.OK)
	@RequestMapping(value = "/auth/signin", method = RequestMethod.POST)
    public ResponseEntity<UserSigninResponse> signin(@Valid @RequestBody UserSigninRequest userSigninRequest) {
		return new ResponseEntity<>(userService.signin(userSigninRequest), HttpStatus.OK);
    }
	
	@ResponseStatus(code = HttpStatus.OK)
	@RequestMapping(value = "/auth/signup", method = RequestMethod.POST)
    public ResponseEntity<UserSignupResponse> signup(@Valid @RequestBody UserSignupRequest userSignupRequest) {
		return new ResponseEntity<>(userService.signup(userSignupRequest), HttpStatus.OK);
    }
}
