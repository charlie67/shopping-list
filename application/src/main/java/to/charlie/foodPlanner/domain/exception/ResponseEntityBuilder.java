package to.charlie.foodPlanner.domain.exception;

import org.springframework.http.ResponseEntity;

public class ResponseEntityBuilder {

	public static ResponseEntity<Object> build(final CustomException customException) {
		return new ResponseEntity<>(customException, customException.getStatus());
	}
}
