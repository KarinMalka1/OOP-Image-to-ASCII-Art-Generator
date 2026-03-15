package Exceptions;

/**
 * Thrown to indicate a problem related to image loading or processing.
 */
public class ImageException extends AsciiArtException {
    /**
     * Constructor with message as a parameter.
     * @param message the message.
     */
    public ImageException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause as parameters.
     * @param message the message.
     * @param cause the cause.
     */
    public ImageException(String message, Throwable cause) {
        super(message, cause);
    }
}