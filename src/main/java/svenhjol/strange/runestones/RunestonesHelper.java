package svenhjol.strange.runestones;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.helper.StringHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.*;

public class RunestonesHelper {
    public static final int NUMBER_OF_RUNES = 26;
    public static final Identifier SPAWN = new Identifier(Strange.MOD_ID, "spawn_point");
    public static Map<UUID, List<Integer>> PLAYER_LEARNED_RUNES = new HashMap<>();

    public static boolean explode(World world, BlockPos pos, @Nullable PlayerEntity player, boolean destroyBlock) {
        if (player != null)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));

        world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 1.25F, Explosion.DestructionType.DESTROY);

        if (destroyBlock)
            world.setBlockState(pos, Blocks.AIR.getDefaultState());

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

    public static boolean playerKnowsBlockPosRunes(PlayerEntity player, BlockPos pos, int limit) {
        List<Integer> required = getRunesFromBlockPos(pos, limit);
        if (required.size() < limit)
            return false;

        if (PlayerHelper.getAbilities(player).creativeMode)
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

    public static List<Integer> getLearnedRunes(PlayerEntity player) {
        return PLAYER_LEARNED_RUNES.getOrDefault(player.getUuid(), ImmutableList.of());
    }

    public static void resetLearnedRunes(PlayerEntity player) {
        UUID uuid = player.getUuid();
        PLAYER_LEARNED_RUNES.remove(uuid);
        PLAYER_LEARNED_RUNES.put(uuid, new ArrayList<>());
    }

    public static void addLearnedRune(PlayerEntity player, int rune) {
        UUID uuid = player.getUuid();
        if (!PLAYER_LEARNED_RUNES.containsKey(uuid))
            PLAYER_LEARNED_RUNES.put(uuid, new ArrayList<>());

        if (!hasLearnedRune(player, rune))
            PLAYER_LEARNED_RUNES.get(uuid).add(rune);
    }

    public static boolean hasLearnedRune(PlayerEntity player, int rune) {
        return getLearnedRunes(player).contains(rune);
    }

    public static void populateLearnedRunes(PlayerEntity player, int[] learned) {
        for (int rune : learned) {
            addLearnedRune(player, rune);
        }
    }

    @Nullable
    public static BlockPos getBlockPosFromItemStack(World world, ItemStack stack) {
        if (stack.getItem() == Items.COMPASS) {

            if (!CompassItem.hasLodestone(stack) || !stack.hasTag() || stack.getTag() == null)
                return null;

            // must be the correct dimension as the lodestone
            Optional<RegistryKey<World>> dimension = CompassItem.getLodestoneDimension(stack.getTag());
            if (!dimension.isPresent() || !DimensionHelper.isDimension(world, dimension.get()))
                return null;

            BlockPos pos = NbtHelper.toBlockPos(stack.getTag().getCompound("LodestonePos"));
            pos = pos.add(0, 1, 0); // the block above the lodestone
            return pos;

        }

        return null;
    }

    public static String getFormattedLocationName(Identifier locationId) {
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
