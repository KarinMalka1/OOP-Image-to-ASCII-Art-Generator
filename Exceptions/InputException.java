package Exceptions;

/**
 * Thrown to indicate an error in the user input or command format.
 * Used when the shell receives invalid arguments, commands, or options.
 */
public class InputException extends AsciiArtException {
    /**
     * Constructor with message as a parameter.
     * @param message the message.
     */
    public InputException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause as parameters.
     * @param message the message.
     * @param cause the cause.
     */
    public InputException(String message, Throwable cause) {
        super(message, cause);
    }
}