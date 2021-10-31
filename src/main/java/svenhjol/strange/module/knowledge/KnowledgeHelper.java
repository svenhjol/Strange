package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import svenhjol.charm.helper.LogHelper;

import java.util.*;

public class KnowledgeHelper {
    public static final String UNKNOWN = "?";

    public static Random getRandom() {
        return new Random(Knowledge.seed);
    }

    public static String generateRunes(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = getRandom();

        for(int i = 0; i < length; ++i) {
            builder.append((char)(random.nextInt(Knowledge.NUM_RUNES) + Knowledge.ALPHABET_START));
        }

        return builder.toString();
    }

    public static long generateSeedFromString(String string) {
        int s = 0;

        for (int i = 0; i < string.length(); i++) {
            s += string.charAt(i);
        }

        Random random = new Random(s);
        return random.nextLong();
    }

    public static String convertRunesWithDecay(String runes, float amount) {
        StringBuilder out = new StringBuilder();
        amount = Mth.clamp(amount, 0.0F, 1.0F);
        Random random = getRandom();

        for(int i = 0; i < runes.length(); ++i) {
            if (random.nextFloat() > amount) {
                out.append(runes.charAt(i));
            } else {
                out.append(UNKNOWN);
            }
        }

        return out.toString();
    }

    public static String convertRunesWithLearnedRunes(String runes, List<Integer> learned) {
        StringBuilder out = new StringBuilder();

        for(int i = 0; i < runes.length(); ++i) {
            int chr = runes.charAt(i) - Knowledge.ALPHABET_START;
            if (learned.contains(chr)) {
                out.append(runes.charAt(i));
            } else {
                out.append(UNKNOWN);
            }
        }

        return out.toString();
    }

    public static String generateRunesFromResource(ResourceLocation res, int length) {
        String namespace = res.getNamespace();
        String first = namespace.substring(0, Math.min(4, namespace.length()));
        String path = res.getPath();
        String out = path + first;

        return generateRunesFromString(out.toLowerCase(Locale.ROOT), length);
    }

    public static String generateRunesFromString(String string, int length) {
        String filtered = string.replaceAll("[^a-zA-Z0-9]", "");
        StringBuilder in = new StringBuilder(filtered);
        StringBuilder out = new StringBuilder();
        int loops = 0;
        Random random = getRandom();

        do {
            if (in.length() >= length) {
                char[] chars = in.toString().toLowerCase(Locale.ROOT).toCharArray();
                random.nextInt();

                for(int i = Math.min(chars.length, length) - 1; i >= 0; --i) {
                    int chr = chars[i];
                    if (chr >= 'a' && chr <= 'z') {
                        int x = chr + random.nextInt(Knowledge.NUM_RUNES);
                        if (x > 122)
                            chr = Mth.clamp(96 + (x - 122), 97, 122);

                        x += random.nextInt(Knowledge.NUM_RUNES / 2);
                        if (x > 122)
                            chr = Mth.clamp(96 + (x - 122), 97, 122);

                        out.append((char)chr);
                    }

                    if (chr >= '0' && chr <= '9') {
                        int x = random.nextInt(Knowledge.NUM_RUNES);
                        chr = Mth.clamp(96 + x, 97, 122);
                        out.append((char)chr);
                    }
                }

                return out.reverse().toString();
            }

            in.append(filtered);
            ++loops;
        } while(loops <= 8);

        throw new RuntimeException("Max loops reached when checking string length");
    }

    public static String generateRandomRunesString(Random random, float difficulty) {
        return generateRandomRunesString(random, difficulty, Knowledge.MIN_LENGTH, Knowledge.MAX_LENGTH);
    }

    public static String generateRandomRunesString(Random random, float difficulty, int minLength, int maxLength) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < maxLength; i++) {
            int chr = Math.min(Knowledge.ALPHABET_END, Math.max(Knowledge.ALPHABET_START, random.nextInt((int) Math.max(6, Knowledge.NUM_RUNES * difficulty)) + Knowledge.ALPHABET_START));
            sb.append((char)chr);
            if (sb.length() > minLength && i / (float) maxLength > difficulty) {
                break;
            }
        }
        return sb.toString();
    }

    public static String generateRunesFromPos(BlockPos pos) {
        long l = pos.asLong();
        boolean negative = l < 0L;
        char[] chars = Long.toString(Math.abs(l), Knowledge.NUM_RUNES).toCharArray();

        for(int i = 0; i < chars.length; ++i) {
            chars[i] = (char)(chars[i] + (chars[i] > '9' ? 10 : 49));
        }

        return (negative ? "a" : "b") + new String(chars);
    }

    public static char getCharFromRange(String range, int index) {
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < range.length(); i++) {
            chars.add(range.charAt(i));
        }

        Collections.shuffle(chars, getRandom());
        if (index < chars.size()) {
            return chars.get(index);
        }

        throw new IndexOutOfBoundsException("Invalid index");
    }

    public static boolean isValidRuneString(String runes) {
        if (runes.length() == 0) {
            return false;
        }

        // get start
        char start = runes.charAt(0);
        Optional<KnowledgeBranch<?, ?>> branch = KnowledgeBranch.getByStartRune(start);
        if (branch.isEmpty()) return false;
        return branch.get().has(runes);
    }

    public static Optional<String> tryGenerateUniqueId(KnowledgeBranch<?, ?> branch, Random random, float difficulty, int minLength, int maxLength) {
        int tries = 0;
        int maxTries = 20;
        boolean foundUniqueRunes = false;
        String runes = "";

        // keep trying to find a unique rune string
        while (!foundUniqueRunes && tries < maxTries) {
            runes = generateRandomRunesString(random, difficulty + (tries * 0.05F), minLength, maxLength);
            foundUniqueRunes = !branch.has(runes);
            ++tries;
        }

        if (!foundUniqueRunes) {
            LogHelper.debug(KnowledgeHelper.class, "Could not calculate unique rune string for this branch, giving up");
            return Optional.empty();
        }

        return Optional.of(branch.getStartRune() + runes);
    }
}
