package ascii_art;

import image.Image;
import image.ImageProcessing;
import image_char_matching.SubImgCharMatcher;
import java.util.Arrays;
import java.io.IOException;

/**
 * AsciiArtAlgorithm
 * Responsible for the logic of converting an image into an ASCII grid.
 * Optimized with caching to prevent recalculating brightness for
 * the same image instance.
 */
public class AsciiArtAlgorithm {

    //Member variables.
    private final Image image;
    private final int resolution;
    private final SubImgCharMatcher matcher;
    private final boolean isReverse;

    private static double[][] cachedBrightnessMatrix = null;
    private static Image cachedImageRef = null;
    private static int cachedResolution = -1;

    /**
     * Constructor
     * @param image The source image.
     * @param resolution The number of characters per row.
     * @param matcher The object responsible for matching brightness to characters.
     */
    public AsciiArtAlgorithm(Image image, int resolution,
                             SubImgCharMatcher matcher,
                             boolean isReverse) {
        this.image = image;
        this.resolution = resolution;
        this.matcher = matcher;
        this.isReverse = isReverse;
    }

    /**
     * Execution method.
     * 1. Checks if brightness data exists in cache.
     * 2. If not, calculates it using your ImageProcessing class.
     * 3. Converts brightness values to characters.
     * @return 2D char array representing the ASCII art.
     */
    public char[][] run() {

        double[][] brightnessMatrix = resolveBrightnessMatrix();

        int rows = brightnessMatrix.length;
        int cols = brightnessMatrix[0].length;
        char[][] asciiArt = new char[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double brightness = brightnessMatrix[row][col];
                if (isReverse) {
                    brightness = 1.0 - brightness;
                }
                asciiArt[row][col] = matcher.getCharByImageBrightness(brightness);
            }
        }

        return asciiArt;
    }

    /**
     * Helper to handle the caching logic and calculation.
     * Checks if the current run parameters match the previous run.
     */
    private double[][] resolveBrightnessMatrix() {

        boolean isSameImage = (this.image == cachedImageRef);
        boolean isSameResolution = (this.resolution == cachedResolution);

        if (isSameImage && isSameResolution && cachedBrightnessMatrix != null) {
            return cachedBrightnessMatrix;
        }

        Image paddedImage = ImageProcessing.padImage(image);
        Image[][] subImages = ImageProcessing.splitImage(paddedImage, resolution);
        int rows = subImages.length;
        int cols = subImages[0].length;
        double[][] newBrightnessMatrix = new double[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                newBrightnessMatrix[row][col] = ImageProcessing.
                        calculateBrightness(subImages[row][col]);
            }
        }

        cachedImageRef = this.image;
        cachedResolution = this.resolution;
        cachedBrightnessMatrix = newBrightnessMatrix;

        return newBrightnessMatrix;
    }
}