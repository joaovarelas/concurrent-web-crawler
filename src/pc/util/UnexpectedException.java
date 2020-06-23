package pc.util;

/**
 * Exception thrown due an unexpected error during execution.
 *
 */
@SuppressWarnings("serial")
public final class UnexpectedException extends RuntimeException {
	/**
	 * Create exception using given message.
	 * 
	 * @param message Error message.
	 */
	public UnexpectedException(String message) {
		super(message);
	}

	/**
	 * Create exception using given message.
	 * 
	 * @param cause Cause of error.
	 */
	public UnexpectedException(Throwable cause) {
		super(cause);
	}
}
