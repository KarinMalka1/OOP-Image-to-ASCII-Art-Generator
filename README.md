Q1) In ascii_art, the class Shell is the interactive command line
UI: it reads user commands (change charset, resolution, output type,
reverse, run), holds the current Image, SubImgCharMatcher, resolution
and output object, and triggers the algorithm. AsciiArtAlgorithm is
the core logic class: it takes an Image, a resolution and a SubImgCharMatcher,
splits the image into sub images, computes their brightness (via
ImageProcessing) and converts each brightness value to a character. In the
image package, Image represents an image (pixels, width, height, save/load),
while ImageProcessing is a static utility class that pads the image, splits 
it into sub images and calculates brightness. In the image_char_matching
package, SubImgCharMatcher is responsible for mapping normalized brightness
values to characters from the active character set and returning the best
match for a given brightness. The relationships between the classes are mainly
composition/“has a”: Shell and AsciiArtAlgorithm each hold references to Image
and SubImgCharMatcher, and AsciiArtAlgorithm also depends on the static services
of ImageProcessing to prepare and analyze the image before character matching.

Q2) We used HashSet<Character> (via Set<Character>) in Shell and
SubImgCharMatcher to store the active character set without duplicates,
because it gives average O(1) time for add, remove and contains, with
O(n) memory. For displaying characters in order I wrapped it with a
TreeSet<Character> in printChars(), since TreeSet keeps elements sorted
(O(n log n) build, O(n) iteration), which is acceptable for occasional printing.
In SubImgCharMatcher I used a TreeMap<Double, List<Character>> to map
normalized brightness values to characters, because TreeMap supports efficient
floorEntry/ceilingEntry in O(log n), which is exactly what I need to find the
closest brightness. I also used a temporary HashMap<Character, Double> to store
raw brightness values before normalization, since it gives O(1) average put/get
and linear iteration. For variable length collections of characters (for ranges
like a-z and groups of chars with the same brightness) I used
ArrayList<Character> / List<Character>, which provide amortized O(1) append and
O(1) indexed access, with O(n) space. Finally, for image data and grids I used
arrays such as char[], double[][], Image[][] and Color[][], because a 2D array
matches the natural row–column structure of images, offers O(1) indexed access,
and has minimal overhead in time and memory when iterating over all pixels.

Q3) We used Java’s exception mechanism to separate normal program flow from
error handling, especially for user input errors and image related problems.
We defined custom runtime exceptions: InputException for invalid user commands
or arguments, and ImageException for problems related to loading or processing
the image. In the shell, methods like handleAdd, handleRemove, handleResolution,
handleOutput and runAlgorithm validate the user input and, if the format is
incorrect or a constraint is violated (e.g. wrong range, invalid resolution,
too small charset), they throw an InputException with a clear error message.
In run, the main command loop wraps the switch in a try/catch (InputException ex)
block and prints ex.getMessage() to the user, so the program continues running
interactively while still reporting errors. For image loading, the constructor
new Image(imageName) is wrapped in a try/catch (IOException) and rethrown as an
ImageException with a descriptive message. In main, I catch the common base type
AsciiArtException and print its message, so any unrecoverable error (input or
image related) results in a clean error message instead of a raw stack trace.
