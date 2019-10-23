package it.islandofcode.jbiblio.gbooks;

public class BookNotFound extends Exception {

	private static final long serialVersionUID = 1L;
	private static final String defaultmex = "Libro non trovato!";
	
	public BookNotFound() {
		super(defaultmex);
	}

	/**
	 * @param message
	 */
	public BookNotFound(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public BookNotFound(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BookNotFound(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public BookNotFound(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
