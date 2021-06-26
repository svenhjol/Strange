package svenhjol.strange.module.runestones;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.*;

public class RunestonesHelper {
    public static final int NUMBER_OF_RUNES = 26;
    public static final ResourceLocation SPAWN = new ResourceLocation(Strange.MOD_ID, "spawn_point");
    public static Map<UUID, List<Integer>> PLAYER_LEARNED_RUNES = new HashMap<>();

    public static boolean explode(Level world, BlockPos pos, @Nullable Player player, boolean destroyBlock) {
        if (player != null)
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20));

        world.explode(null, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 1.25F, Explosion.BlockInteraction.DESTROY);

        if (destroyBlock)
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

        return false;
    }

    public static List<Integer> getRunesFromBlockPos(BlockPos pos, int limit) {
        if (pos == null)
            return ImmutableList.of();

        String s = blockPosToBase26(pos);
        List<Integer> runes = new ArrayList<>();

        for (int i = 0; i < s.length(); i++) {
            runes.add(((int)s.charAt(i)) - 97);
        }

        if (limit > 0)
            return runes.subList(0, Math.min(runes.size(), limit));

        return runes;
    }

    public static boolean playerKnowsBlockPosRunes(Player player, BlockPos pos, int limit) {
        List<Integer> required = getRunesFromBlockPos(pos, limit);
        if (required.size() < limit)
            return false;

        if (PlayerHelper.getAbilities(player).instabuild)
            return true;

        List<Integer> learned = getLearnedRunes(player);
        if (learned.size() == 0)
            return false;

        for (int rune : required) {
            if (!learned.contains(rune))
                return false;
        }

        return true;
    }

    public static List<Integer> getLearnedRunes(Player player) {
        return PLAYER_LEARNED_RUNES.getOrDefault(player.getUUID(), ImmutableList.of());
    }

    public static void resetLearnedRunes(Player player) {
        UUID uuid = player.getUUID();
        PLAYER_LEARNED_RUNES.remove(uuid);
        PLAYER_LEARNED_RUNES.put(uuid, new ArrayList<>());
    }

    public static void addLearnedRune(Player player, int rune) {
        UUID uuid = player.getUUID();
        if (!PLAYER_LEARNED_RUNES.containsKey(uuid))
            PLAYER_LEARNED_RUNES.put(uuid, new ArrayList<>());

        if (!hasLearnedRune(player, rune))
            PLAYER_LEARNED_RUNES.get(uuid).add(rune);

        if (player instanceof ServerPlayer && RunestonesHelper.getLearnedRunes(player).size() >= NUMBER_OF_RUNES)
            Runestones.triggerLearnedAllRunes((ServerPlayer)player);
    }

    public static boolean hasLearnedRune(Player player, int rune) {
        return getLearnedRunes(player).contains(rune);
    }

    public static void populateLearnedRunes(Player player, int[] learned) {
        for (int rune : learned) {
            addLearnedRune(player, rune);
        }
    }

    public static String getFormattedLocationName(ResourceLocation locationId) {
        return StringHelper.capitalize(locationId.getPath().replaceAll("_", " "));
    }

    /**
     * @link https://stackoverflow.com/a/41733499
     */
    private static String blockPosToBase26(BlockPos pos) {
        long l = Math.abs(pos.asLong());

        char[] str = Long.toString(l, 26).toCharArray();
        for (int i = 0; i < str.length; i++) {
            str[i] += str[i] > '9' ? 10 : 49;
        }
        return new String(str);
    }
}
