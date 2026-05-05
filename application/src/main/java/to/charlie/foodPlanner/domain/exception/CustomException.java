package to.charlie.foodPlanner.domain.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class CustomException {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;

	private HttpStatus status;
	private String message;

	public CustomException(final LocalDateTime timestamp, final HttpStatus status, final String message) {
		this.timestamp = timestamp;
		this.status = status;
		this.message = message;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(final HttpStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "CustomException [timestamp="
						+ timestamp
						+ ", status="
						+ status
						+ ", message="
						+ message
						+ "]";
	}
}
