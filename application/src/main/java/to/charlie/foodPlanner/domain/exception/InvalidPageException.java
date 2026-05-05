package to.charlie.foodPlanner.domain.exception;

public class InvalidPageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPageException(final String message) {
		super(message);
	}
}
