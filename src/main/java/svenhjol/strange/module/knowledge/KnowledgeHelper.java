package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

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
            builder.append((char)(random.nextInt(Knowledge.NUM_RUNES) + 97));
        }

        return builder.toString();
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
            int chr = runes.charAt(i) - 97;
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

    public static String generateDestinationRunes(Random random, float difficulty) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < KnowledgeData.MAX_LENGTH; i++) {
            int chr = Math.min(122, Math.max(97, random.nextInt((int) Math.max(6, Knowledge.NUM_RUNES * difficulty)) + 97));
            sb.append((char)chr);
            if (sb.length() > KnowledgeData.MIN_LENGTH && i / (float) KnowledgeData.MAX_LENGTH > difficulty) {
                break;
            }
        }
        return sb.toString();
    }

    public static String generateRunesFromPos(BlockPos pos) {
        KnowledgeData knowledgeData = Knowledge.getSavedData().orElseThrow();
        long l = pos.asLong();
        boolean negative = l < 0L;
        char[] chars = Long.toString(Math.abs(l), Knowledge.NUM_RUNES).toCharArray();

        for(int i = 0; i < chars.length; ++i) {
            chars[i] = (char)(chars[i] + (chars[i] > '9' ? 10 : 49));
        }

        return (negative ? knowledgeData.NEGATIVE_RUNE : knowledgeData.POSITIVE_RUNE) + new String(chars);
    }

    public static List<ItemStack> generateItemsFromRunes(ServerLevel level, BlockPos pos, Entity entity, ResourceLocation loot) {
        Random random = new Random(pos.asLong());

        LootTable lootTable = level.getServer().getLootTables().get(loot);
        List<ItemStack> list = lootTable.getRandomItems(new LootContext.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withRandom(random)
            .create(LootContextParamSets.CHEST));

        return list;
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
        KnowledgeData knowledge = Knowledge.getSavedData().orElseThrow();

        if (runes.length() == 0) {
            return false;
        }

        // get start
        char first = runes.charAt(0);

        if (first == knowledge.SPAWN_RUNE) {
            return runes.length() == 1;
        }
        if (first == knowledge.BIOME_RUNE) {
            return knowledge.getBiomes().containsValue(runes);
        }
        if (first == knowledge.DESTINATION_RUNE) {
            return knowledge.getDestinations().containsKey(runes);
        }
        if (first == knowledge.DIMENSION_RUNE) {
            return knowledge.getDimensions().containsValue(runes);
        }
        if (first == knowledge.PLAYER_RUNE) {
            return knowledge.getPlayers().containsValue(runes);
        }
        if (first == knowledge.STRUCTURE_RUNE) {
            return knowledge.getStructures().containsValue(runes);
        }
        if (first == knowledge.LOCATION_RUNE) {
            return runes.length() >= KnowledgeData.MIN_LENGTH;
        }

        return false;
    }
}
