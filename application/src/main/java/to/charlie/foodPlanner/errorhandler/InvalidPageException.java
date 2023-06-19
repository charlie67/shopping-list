package to.charlie.foodPlanner.errorhandler;

public class InvalidPageException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidPageException(final String message) {
    super(message);
  }
}
