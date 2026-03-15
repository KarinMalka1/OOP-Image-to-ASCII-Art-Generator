package image;

import java.awt.Color;

/**
 * A utility class for processing Image objects.
 * Handles padding, splitting, and brightness calculation.
 */
public class ImageProcessing {

    // Class constants
    private static final int MAX_RGB_VALUE = 255;
    private static final double RED_WEIGHT = 0.2126;
    private static final double GREEN_WEIGHT = 0.7152;
    private static final double BLUE_WEIGHT = 0.0722;

    /**
     * 1.4.2.1 Pad the image with white pixels so its dimensions are powers of 2.
     * The padding is symmetrical (centered).
     * @param image The source image.
     * @return A new Image object with dimensions padded to the nearest power of 2.
     */
    public static Image padImage(Image image) {
        int currentHeight = image.getHeight();
        int currentWidth = image.getWidth();

        int newHeight = nextPowerOfTwo(currentHeight);
        int newWidth = nextPowerOfTwo(currentWidth);

        if (newHeight == currentHeight && newWidth == currentWidth) {
            return image;
        }

        int rowOffset = (newHeight - currentHeight) / 2;
        int colOffset = (newWidth - currentWidth) / 2;
        Color[][] newPixelArray = new Color[newHeight][newWidth];
        for (int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                boolean inVerticalBounds = (i >= rowOffset) &&
                        (i < rowOffset + currentHeight);
                boolean inHorizontalBounds = (j >= colOffset) &&
                        (j < colOffset + currentWidth);
                if (inVerticalBounds && inHorizontalBounds) {
                    newPixelArray[i][j] = image.getPixel(i - rowOffset,
                            j - colOffset);
                } else {
                    newPixelArray[i][j] = Color.WHITE;
                }
            }
        }
        return new Image(newPixelArray, newWidth, newHeight);
    }

    /**
     * 1.4.2.2 Split the image into sub-images based on resolution.
     * @param image The padded image (dimensions must be powers of 2).
     * @param resolution The number of sub-images in a row.
     * @return A 2D array of Image objects representing the sub-images.
     */
    public static Image[][] splitImage(Image image, int resolution) {
        int width = image.getWidth();
        int height = image.getHeight();

        int subImageSize = width / resolution;

        int rows = height / subImageSize;
        int cols = resolution;

        Image[][] subImages = new Image[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color[][] subImagePixels = new Color[subImageSize][subImageSize];
                for (int i = 0; i < subImageSize; i++) {
                    for (int j = 0; j < subImageSize; j++) {
                        int globalY = (row * subImageSize) + i;
                        int globalX = (col * subImageSize) + j;
                        subImagePixels[i][j] = image.getPixel(globalY, globalX);
                    }
                }
                subImages[row][col] = new Image(subImagePixels,
                        subImageSize, subImageSize);
            }
        }
        return subImages;
    }

    /**
     * Helper method to find the nearest power of 2
     * greater than or equal to n.
     */
    private static int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    /**
     * Calculate the brightness of an image.
     * Converts each pixel to grey using the formula:
     * grey = 0.2126 * R + 0.7152 * G + 0.0722 * B
     * Then normalizes by the total number of pixels and 255.
     * @param image The image to calculate brightness for.
     * @return A double between 0.0 and 1.0 representing
     * the average brightness.
     */
    public static double calculateBrightness(Image image) {
        double totalGreyScaleValue = 0;
        int height = image.getHeight();
        int width = image.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = image.getPixel(i, j);
                double greyPixel = c.getRed() * RED_WEIGHT +
                        c.getGreen() * GREEN_WEIGHT +
                        c.getBlue() * BLUE_WEIGHT;
                totalGreyScaleValue += greyPixel;
            }
        }
        return totalGreyScaleValue / (width * height * MAX_RGB_VALUE);
    }
}