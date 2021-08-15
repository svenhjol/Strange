package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import svenhjol.strange.module.journals.JournalsData;

import java.util.Locale;
import java.util.Random;

public class KnowledgeHelper {
    public static final String UNKNOWN = "?";

    public static Random getRandom() {
        return new Random(Knowledge.seed);
    }

    public static String generate(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = getRandom();

        for(int i = 0; i < length; ++i) {
            builder.append((char)(random.nextInt(Knowledge.NUM_RUNES) + 97));
        }

        return builder.toString();
    }

    public static String convertWithDegradation(String string, float amount) {
        StringBuilder out = new StringBuilder();
        amount = Mth.clamp(amount, 0.0F, 1.0F);
        Random random = getRandom();

        for(int i = 0; i < string.length(); ++i) {
            if (random.nextFloat() > amount) {
                out.append(string.charAt(i));
            } else {
                out.append(UNKNOWN);
            }
        }

        return out.toString();
    }

    public static String convertWithLearnedRunes(String string, JournalsData playerJournal) {
        StringBuilder out = new StringBuilder();

        for(int i = 0; i < string.length(); ++i) {
            int chr = string.charAt(i) - 97;
            if (playerJournal.getLearnedRunes().contains(chr)) {
                out.append(string.charAt(i));
            } else {
                out.append(UNKNOWN);
            }
        }

        return out.toString();
    }

    public static String generateFromResource(ResourceLocation res, int length) {
        String namespace = res.getNamespace();
        String first = namespace.substring(0, Math.min(4, namespace.length()));
        String path = res.getPath();
        String out = path + first;

        return generateFromString(out.toLowerCase(Locale.ROOT), length);
    }

    public static String generateFromString(String string, int length) {
        String filtered = string.replaceAll("[^a-zA-Z]", "");
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
                            chr = 96 + (x - 122);

                        x += random.nextInt(13);
                        if (x > 122) {
                            chr = Math.min(122, 96 + (x - 122));
                        }

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

    public static String convertFromBlockPos(BlockPos pos) {
        long l = pos.asLong();
        boolean negative = l < 0L;
        char[] chars = Long.toString(Math.abs(l), Knowledge.NUM_RUNES).toCharArray();

        for(int i = 0; i < chars.length; ++i) {
            chars[i] = (char)(chars[i] + (chars[i] > '9' ? 10 : 49));
        }

        return (negative ? "y" : "z") + new String(chars);
    }
}
