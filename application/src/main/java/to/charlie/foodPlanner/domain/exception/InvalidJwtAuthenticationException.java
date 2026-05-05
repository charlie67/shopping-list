package to.charlie.foodPlanner.domain.exception;

public class InvalidJwtAuthenticationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidJwtAuthenticationException(final String message) {
		super(message);
	}
}
