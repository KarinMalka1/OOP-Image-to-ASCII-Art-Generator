package Exceptions;

/**
 * Base class for all custom exceptions in the ASCII art application.
 */
public class AsciiArtException extends RuntimeException {
    /**
     * Constructor with message as a parameter.
     * @param message the message.
     */
    public AsciiArtException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause as parameters.
     * @param message the message.
     * @param cause the cause.
     */
    public AsciiArtException(String message, Throwable cause) {
        super(message, cause);
    }
}