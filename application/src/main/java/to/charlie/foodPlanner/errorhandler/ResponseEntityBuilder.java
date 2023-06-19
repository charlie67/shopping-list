package to.charlie.foodPlanner.errorhandler;

import org.springframework.http.ResponseEntity;

public class ResponseEntityBuilder {

  public static ResponseEntity<Object> build(final CustomException customException) {
    return new ResponseEntity<>(customException, customException.getStatus());
  }
}
