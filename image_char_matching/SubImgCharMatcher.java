package image_char_matching;

import Exceptions.InputException;

import java.util.*;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * Maps sub-image brightness values to characters from a configurable charset.
 * Precomputes and normalizes per-character brightness so the closest-matching
 * character can be chosen for each sub-image in the ASCII art algorithm.
 */
public class SubImgCharMatcher {

    // Class Constants.
    private static final String EMPTY_SET_OF_CHAR =
            "Character set is empty.";
    private static final String NO_CHAR_MATCH_FOUND =
            "No matching character found.";

    private static final double CHAR_ARRAY_SIZE = 16 * 16;
    private static final double ROWS = 16;
    private static final double COLS = 16;
    private static final int SIZE_TWO = 2;


    // Member variables.
    private final TreeMap<Double, List<Character>>
            normalizedBrightnessToChar = new TreeMap<>();

    private final Set<Character> charSet = new HashSet<>();


    /**
     * Constructs a SubImgCharMatcher with an initial set of characters.
     * @param charset The initial array of characters
     * to use for the ASCII art algorithm.
     */
    public SubImgCharMatcher(char[] charset) {
        if (charset != null) {
            for (char c : charset) {
                addChar(c);
            }
        }

        normalizeBrightness();
    }

    /**
     * Adds a character to the set and renormalizes the brightness map.
     * @param c The character to add.
     */
    public void addChar(char c) {
        if (!charSet.contains(c)) {
            charSet.add(c);
            normalizeBrightness();
        }
    }

    /**
     * Removes a character from the set and renormalizes the brightness map.
     * @param c The character to remove.
     */
    public void removeChar(char c) {
        if (charSet.contains(c)) {
            charSet.remove(c);
            normalizeBrightness();
        }
    }

    /**
     * Given a brightness value (0-1) of a sub image, returns
     * the ASCII character from the set whose normalized brightness
     * is closest (in absolute value) to the given brightness.
     * In case of a tie in brightness, the character with the
     * smallest ASCII value is returned.
     * @param brightness The brightness value of the sub
     * image (double, typically 0.0 to 1.0).
     * @return The best matching ASCII character.
     */
    public char getCharByImageBrightness(double brightness) {
        if (normalizedBrightnessToChar.isEmpty()) {
            throw new InputException(EMPTY_SET_OF_CHAR);
        }

        Map.Entry<Double, List<Character>> floorEntry =
                normalizedBrightnessToChar.floorEntry(brightness);

        Map.Entry<Double, List<Character>> ceilingEntry =
                normalizedBrightnessToChar.ceilingEntry(brightness);

        // Perfect match.
        if (floorEntry != null && floorEntry.getKey().equals(brightness)) {
            return getSmallestAsciiChar(floorEntry.getValue());
        }

        // Only floor.
        if (floorEntry != null && ceilingEntry == null) {
            return getSmallestAsciiChar(floorEntry.getValue());
        }

        // Only ceiling.
        if (floorEntry == null && ceilingEntry != null) {
            return getSmallestAsciiChar(ceilingEntry.getValue());
        }

        // Both exist.
        if (floorEntry != null && ceilingEntry != null) {
            double floorDiff = Math.abs(brightness - floorEntry.getKey());
            double ceilDiff = Math.abs(brightness - ceilingEntry.getKey());

            if (floorDiff < ceilDiff) {
                return getSmallestAsciiChar(floorEntry.getValue());
            } else if (ceilDiff < floorDiff) {
                return getSmallestAsciiChar(ceilingEntry.getValue());
            } else {

                char floorChar = getSmallestAsciiChar(floorEntry.getValue());
                char ceilChar = getSmallestAsciiChar(ceilingEntry.getValue());

                return (floorChar <= ceilChar) ? floorChar : ceilChar;
            }
        }

        throw new InputException(NO_CHAR_MATCH_FOUND);
    }


    /*
     * Calculates the raw brightness (0 to 1) of a single character.
     */
    private double calculateRawCharBrightness(char c) {
        boolean[][] booleanMatrix = CharConverter.convertToBoolArray(c);
        double trueCounter = 0;
        for(int row = 0; row < ROWS; row++){
            for(int col = 0; col < COLS; col++){
                if(booleanMatrix[row][col]){
                    trueCounter++;
                }
            }
        }
        return trueCounter / CHAR_ARRAY_SIZE;
    }

    /*
     * Recalculates and normalizes the brightness of all characters in the set.
     */
    private void normalizeBrightness() {
        normalizedBrightnessToChar.clear();

        if (charSet.size() < SIZE_TWO) {
            if (charSet.size() == 1) {
                char onlyChar = charSet.iterator().next();
                double rawBrightness = calculateRawCharBrightness(onlyChar);
                normalizedBrightnessToChar.put(rawBrightness,
                        Collections.singletonList(onlyChar));
            }
            return;
        }

        Map<Character, Double> rawBrightnessMap = new HashMap<>();
        double minBrightness = Double.MAX_VALUE;
        double maxBrightness = Double.MIN_VALUE;

        for (char c : charSet) {
            double rawBrightness = calculateRawCharBrightness(c);
            rawBrightnessMap.put(c, rawBrightness);
            minBrightness = Math.min(minBrightness, rawBrightness);
            maxBrightness = Math.max(maxBrightness, rawBrightness);
        }

        double brightnessRange = maxBrightness - minBrightness;

        for (Map.Entry<Character, Double> entry : rawBrightnessMap.entrySet()) {
            char c = entry.getKey();
            double rawBrightness = entry.getValue();
            double normalizedBrightness;
            if (brightnessRange == 0) {
                normalizedBrightness = 0.5;
            } else {
                normalizedBrightness = (rawBrightness - minBrightness)
                        / brightnessRange;
            }

            normalizedBrightnessToChar
                    .computeIfAbsent(normalizedBrightness,
                            k -> new ArrayList<>())
                    .add(c);
        }

        for (List<Character> charList : normalizedBrightnessToChar.values()) {
            charList.sort(Comparator.naturalOrder());
        }
    }

    /*
     * Helper to return the character with the smallest ASCII value from a list.
     */
    private char getSmallestAsciiChar(List<Character> charList) {
        return charList.get(0);
    }
}



