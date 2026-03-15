package ascii_art;

import Exceptions.AsciiArtException;
import Exceptions.ImageException;
import Exceptions.InputException;
import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;
import image_char_matching.SubImgCharMatcher;

import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Shell provides an interactive command line interface for configuring and
 * running the ASCII art algorithm on a given image.
 */
public class Shell {

    // Class constants.
    private static final int DEFAULT_RESOLUTION = 2;
    private static final String DEFAULT_ABSOLUTE_VALUE = "abs";
    private static final char[] DEFAULT_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int DIFFERENCE_BETWEEN_CHARS = 95;
    private static final int STARTING_POINT = 32;
    private static final int RESOLUTION_STEP_FACTOR = 2;
    private static final int RANGE_EXPRESSION_LENGTH = 3;
    private static final int RANGE_SEPARATOR_INDEX = 1;
    private static final char RANGE_SEPARATOR_CHAR = '-';


    private static final String USER_INPUT_PREFIX = ">>> ";
    private static final String CHAR_RANGE_ALL_KEYWORD = "all";
    private static final String CHAR_RANGE_SPACE_KEYWORD = "space";
    private static final String RESOLUTION_CMD_UP = "up";
    private static final String RESOLUTION_CMD_DOWN = "down";
    private static final String OUTPUT_MODE_HTML = "html";
    private static final String OUTPUT_MODE_CONSOLE = "console";
    private static final String HTML_OUTPUT_FILE_NAME = "out.html";
    private static final String HTML_OUTPUT_FONT_NAME = "Courier New";
    private static final String PRINT_CHARS_SUFFIX = " ";

    private static final String CMD_EXIT = "exit";
    private static final String CMD_CHARS = "chars";
    private static final String CMD_ADD = "add";
    private static final String CMD_REMOVE = "remove";
    private static final String CMD_RES = "res";
    private static final String CMD_OUTPUT = "output";
    private static final String CMD_REVERSE = "reverse";
    private static final String CMD_START = "asciiArt";

    private static final String ERR_ADD_FORMAT =
            "Did not add due to incorrect format.";
    private static final String ERR_REMOVE_FORMAT =
            "Did not remove due to incorrect format.";
    private static final String ERR_RES_FORMAT =
            "Did not change resolution due to incorrect format.";
    private static final String ERR_RES_BOUNDARIES =
            "Did not change resolution due to exceeding boundaries." ;
    private static final String ERR_OUTPUT_FORMAT =
            "Did not change output method due to incorrect format.";
    private static final String ERR_CHARSET_TOO_SMALL =
            "Did not execute, charset is too small";
    private static final String ERR_INCORRECT_COMMAND =
            "Did not execute due to incorrect command.";
    private static final String ERR_MISSING_IMAGE =
            "USAGE: java ascii_art.Shell <image_path>";


    // Member variables.
    private Image image;
    private int resolution;
    private final SubImgCharMatcher charMatcher;
    private AsciiOutput output;
    private String roundingType;
    private final Set<Character> charSet = new HashSet<>();

    private boolean isReverse;

    /**
     * Constructs a Shell with default settings, including the default
     * character set (digits 0–9), default resolution, console output,
     * and non reversed brightness mapping.
     */
    public Shell(){
        for (char c : DEFAULT_CHARS) {
            charSet.add(c);
        }
        this.resolution = DEFAULT_RESOLUTION;
        this.charMatcher = new SubImgCharMatcher(DEFAULT_CHARS);
        this.output = new ConsoleAsciiOutput();
        this.roundingType = DEFAULT_ABSOLUTE_VALUE;
        this.isReverse = false;
    }

    /**
     * Starts the interactive shell loop for the given image file.
     * Loads the image and then repeatedly reads and handles user commands
     * until the user exits.
     *
     * @param imageName the path to the image file to be processed
     * @throws ImageException if the image fails to load
     */
    public void run(String imageName) {
        try {
            this.image = new Image(imageName);
        } catch (IOException e) {
            throw new ImageException("Failed to load image: " +
                    e.getMessage(), e);
        }
        while (true) {
            System.out.print(USER_INPUT_PREFIX);
            String input = KeyboardInput.readLine();
            if (input == null) {
                return;
            }
            if (input.isEmpty()) {                
                continue;
            }

            String[] tokens = input.split(" ");
            String command = tokens[0];
            String param =  tokens.length > 1 ? tokens[1] : "";

            try {
                switch (command) {
                    case CMD_EXIT:
                        return;
                    case CMD_CHARS:
                        printChars();
                        break;
                    case CMD_ADD:
                        handleAdd(param);
                        break;
                    case CMD_REMOVE:
                        handleRemove(param);
                        break;
                    case CMD_RES:
                        handleResolution(tokens);
                        break;
                    case CMD_REVERSE:
                        handleReverse();
                        break;
                    case CMD_OUTPUT:
                        handleOutput(param);
                        break;
                    case CMD_START:
                        runAlgorithm();
                        break;
                    default:
                        throw new InputException(ERR_INCORRECT_COMMAND);
                }
            } catch (InputException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /*
     * Prints the currently active character set in sorted order,
     * each character followed by a space, then a newline.
     */
    private void printChars() {
        new TreeSet<>(charSet).forEach(c ->
                System.out.println(c + PRINT_CHARS_SUFFIX));
        System.out.println();
    }

    /*
     * Handles the "add" command by parsing a character or range specification
     * and adding the resulting characters to the active character set and
     * the underlying matcher.
     */
    private void handleAdd(String param) {
        ArrayList<Character> chars = parseCharRange(param);
        if (chars == null) {
            throw new InputException(ERR_ADD_FORMAT);
        }
        for (Character c : chars) {
            if (charSet.add(c)) {
                charMatcher.addChar(c);
            }
        }
    }

    /*
     * Handles the "remove" command by parsing a character or range specification
     * and removing the resulting characters from the active character set and
     * the underlying matcher.
     */
    private void handleRemove(String param) {
        ArrayList<Character> chars = parseCharRange(param);
        if (chars == null) {
            throw new InputException(ERR_REMOVE_FORMAT);
        }
        for (Character c : chars) {
            if (charSet.remove(c)) {
                charMatcher.removeChar(c);
            }
        }
    }

    /*
     * Parses a character argument into a list of characters. Supports:
     * - "all": all printable ASCII characters in the configured range.
     * - "space": the space character.
     * - a single character.
     * - a range in the form "<start>-<end>" (inclusive).
     * Returns null if the argument format is invalid.
     */
    private ArrayList<Character> parseCharRange(String arg) {
        if (arg.isEmpty()) {
            return null;
        }
        if (arg.equals(CHAR_RANGE_ALL_KEYWORD)) {
            ArrayList<Character> all = new ArrayList<>();
            for (int i = 0; i < DIFFERENCE_BETWEEN_CHARS;  i++) {
                all.add((char) (i + STARTING_POINT));
            }
            return all;
        }
        if (arg.equals(CHAR_RANGE_SPACE_KEYWORD)) {
            ArrayList<Character> result = new ArrayList<>();
            result.add(' ');
            return result;
        }
        if (arg.length() == 1) {
            ArrayList<Character> result = new ArrayList<>();
            result.add(arg.charAt(0));
            return result;
        }
        if (arg.length() == RANGE_EXPRESSION_LENGTH &&
                arg.charAt(RANGE_SEPARATOR_INDEX) == RANGE_SEPARATOR_CHAR) {
            char start = arg.charAt(0);
            char end = arg.charAt(2);
            if (start > end) {
                char temp = start;
                start = end;
                end = temp;
            }
            ArrayList<Character> range = new ArrayList<>();
            for (int i = 0; i < (end - start + 1); i++) {
                range.add((char) (start + i));
            }
            return range;
        }
        return null;
    }

    /*
     * Handles the "res" command, which either prints the current resolution
     * or adjusts it up/down by a fixed factor, while ensuring it remains
     * within the allowed image based boundaries.
     */
    private void handleResolution(String[] tokens) {
        if (tokens.length == 1) {
            System.out.println("Resolution set to " + resolution + ".");
            return;
        }
        String cmd = tokens[1];
        int newResolution = resolution;

        if (cmd.equals(RESOLUTION_CMD_UP)) {
            newResolution *= RESOLUTION_STEP_FACTOR;
        } else if (cmd.equals(RESOLUTION_CMD_DOWN)) {
            newResolution /= RESOLUTION_STEP_FACTOR;
        } else {
            throw new InputException(ERR_RES_FORMAT);
        }

        // Boundary checks
        int minChars = Math.max(1, image.getWidth() / image.getHeight());
        int maxChars = image.getWidth();

        if (newResolution < minChars || newResolution > maxChars) {
            throw new InputException(ERR_RES_BOUNDARIES);
        } else {
            resolution = newResolution;
            System.out.println("Resolution set to " + resolution + ".");
        }
    }

    /*
     * Toggles the reverse mode flag, which controls whether brightness
     * is inverted when generating the ASCII art.
     */
    private void handleReverse() {
        isReverse = !isReverse;
    }

    /*
     * Handles the "output" command and switches the output method
     * between HTML and console modes. Throws an InputException if
     * an unsupported mode is provided.
     */
    private void handleOutput(String param) {
        if (param.equals(OUTPUT_MODE_HTML)) {
            output = new HtmlAsciiOutput(HTML_OUTPUT_FILE_NAME,
                    HTML_OUTPUT_FONT_NAME);
        } else if (param.equals(OUTPUT_MODE_CONSOLE)) {
            output = new ConsoleAsciiOutput();
        } else {
            throw new InputException(ERR_OUTPUT_FORMAT);
        }
    }

    /*
     * Executes the ASCII art algorithm with the current settings and
     * sends the resulting character matrix to the configured output.
     * Requires at least two characters in the active character set.
     */
    private void runAlgorithm() {
        if (charSet.size() < 2) {
            throw new InputException(ERR_CHARSET_TOO_SMALL);
        }
        // Pass the 'isReverse' flag to the Algorithm
        AsciiArtAlgorithm algorithm = new AsciiArtAlgorithm(image,
                resolution, charMatcher, isReverse);
        output.out(algorithm.run());
    }



    /**
     * Entry point of the program.
     * Parses the command line argument for the image path and runs the Shell.
     * @param args Command line arguments. args[0] should be the image path.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            throw new InputException(ERR_MISSING_IMAGE);
        }

        String imagePath = args[0];

        try {
            Shell shell = new Shell();
            shell.run(imagePath);
        } catch (AsciiArtException e) {
            System.out.println(e.getMessage());
        }
    }
}
