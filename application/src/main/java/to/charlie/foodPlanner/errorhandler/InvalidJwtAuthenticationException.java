package to.charlie.foodPlanner.errorhandler;

public class InvalidJwtAuthenticationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidJwtAuthenticationException(final String message) {
    super(message);
  }
}
