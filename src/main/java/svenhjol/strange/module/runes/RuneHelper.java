package svenhjol.strange.module.runes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import svenhjol.strange.module.knowledge2.Knowledge2;
import svenhjol.strange.module.runes.exception.RuneStringException;

import java.util.*;

public class RuneHelper {
    public static final char UNKNOWN_CHAR = '?';

    /**
     * Fetches an RNG based on the seed of the loaded overworld or a fresh one if the seed isn't set.
     */
    public static Random getRandom() {
        return Knowledge2.SEED == Long.MIN_VALUE ? new Random() : new Random(Knowledge2.SEED);
    }

    /**
     * Fetch a character from a "rune set" at the specified index.
     * The rune set is shuffled according to the world seed.
     */
    public static char getFromRuneSet(Tier tier, int index) {
        Random random = getRandom();
        List<Character> chars = new ArrayList<>(tier.getChars());

        if (index >= chars.size()) {
            throw new IndexOutOfBoundsException("Invalid index");
        }

        Collections.shuffle(chars, random);
        return chars.get(index);
    }

    /**
     * Generate a rune string from a given resource ID.
     */
    public static String getFromResource(ResourceLocation id, int length) {
        String namespace = id.getNamespace();
        String path = id.getPath();
        String firstFour = namespace.substring(0, Math.min(4, namespace.length()));
        String out = path + firstFour;

        return getFromString(out.toLowerCase(Locale.ROOT), length);
    }

    /**
     * Generate a set of unique runes within a given branch.
     * The branch's start rune will be prepended to the string.
     * There is a tiny and unrealistic chance that this method produces a non-unique set of runes.
     */
    public static String uniqueRunes(RuneBranch<?, ?> branch, Random random, float difficulty, int minLength, int maxLength) {
        boolean hasUnique = false;
        String runes = "";

        for (int tries = 0; tries < 20; tries++) {
            runes = randomRunes(random, difficulty + (tries * 0.05F), minLength, maxLength);

            hasUnique = !branch.contains(branch.getStartRune() + runes);
            if (hasUnique) break;

            random = new Random();
        }

        if (!hasUnique || runes.isEmpty()) {
            runes = randomRunes(new Random(), 1.0F, Runes.MIN_PHRASE_LENGTH, Runes.MAX_PHRASE_LENGTH);
        }

        return branch.getStartRune() + runes;
    }

    /**
     * Converts each character of an input string according to a list of known char ints.
     * If the character is known, it is unmodified.  If not known, it is converted to a "?".
     * Example: an input of "abcdef" with a known list of (0, 2, 3) would output "a?cd??".
     */
    public static String revealRunes(String input, List<Integer> known) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            int chr = input.charAt(i) - Runes.FIRST_RUNE;
            if (known.contains(chr)) {
                out.append(input.charAt(i));
            } else {
                out.append(UNKNOWN_CHAR);
            }
        }

        return out.toString();
    }

    /**
     * Generate a random string of runes.
     * The difficulty determines the length and range of characters.
     * A difficulty of 0.0F produces a string of length minLength and characters within the NOVICE tier.
     */
    public static String randomRunes(Random random, float difficulty, int minLength, int maxLength) {
        int alphaStart = Runes.FIRST_RUNE;
        int alphaEnd = Runes.LAST_RUNE;
        int minLetters = Tier.NOVICE.getChars().size();
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < maxLength; i++) {
            // Generate a random character. The difficulty increases the range of possible letters.
            int chr = Math.min(alphaEnd, Math.max(alphaStart, random.nextInt((int)Math.max(minLetters, Runes.NUM_RUNES * difficulty)) + alphaStart));
            out.append((char)chr);

            // The difficulty also increases the length of the string.
            if (out.length() > minLength && i / (float)maxLength > difficulty) {
                break;
            }
        }

        return out.toString();
    }

    /**
     * Generate a set of runes for a given input string.
     * The string is filtered to make it alphanumeric.
     * Each character of the string is shifted through the alphabet randomly.
     */
    public static String getFromString(String input, int length) {
        int alphaStart = Runes.FIRST_RUNE;
        int alphaEnd = Runes.LAST_RUNE;
        Random random = getRandom();

        String filtered = input.replaceAll("[^a-zA-Z0-9]", "");
        StringBuilder in = new StringBuilder(filtered);
        StringBuilder out = new StringBuilder();

        for (int tries = 0; tries < 9; tries++) {

            if (in.length() >= length) {
                random.nextInt();
                char[] chars = in.toString().toLowerCase(Locale.ROOT).toCharArray();

                // work over the string backwards by character
                for (int i = Math.min(chars.length, length) - 1; i >= 0; --i) {
                    int chr = chars[i];

                    if (chr >= Runes.FIRST_RUNE && chr <= Runes.LAST_RUNE) {
                        // shift the char with a random number of the total runes, wrapping around if it goes out of bounds
                        int ri = chr + random.nextInt(Runes.NUM_RUNES);
                        if (ri > alphaEnd) {
                            chr = Mth.clamp(alphaStart + (ri - alphaEnd), alphaStart + 1, alphaEnd);
                        }

                        // shift the char again with a random number of half the total runes, wrapping again as necessary
                        ri += random.nextInt(Runes.NUM_RUNES / 2);
                        if (ri > alphaEnd) {
                            chr = Mth.clamp(alphaStart + (ri - alphaEnd), alphaStart + 1, alphaEnd);
                        }

                        out.append((char)chr);
                    }
                }

                return out.reverse().toString();
            }

            // Keep adding the input string to the end of the builder to bring the length up.
            in.append(filtered);
        }

        throw new RuneStringException("Maximum loops reached when checking string length");
    }
}
