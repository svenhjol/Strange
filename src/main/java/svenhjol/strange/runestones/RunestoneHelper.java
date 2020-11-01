package svenhjol.strange.runestones;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import svenhjol.charm.base.helper.StringHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.Runestones;

import javax.annotation.Nullable;
import java.util.*;

public class RunestoneHelper {
    public static final int NUMBER_OF_RUNES = 26;
    public static final Identifier SPAWN = new Identifier(Strange.MOD_ID, "spawn_point");
    public static Map<UUID, List<Integer>> PLAYER_DISCOVERIES = new HashMap<>();

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

    public static List<Integer> getDiscoveredRunes(PlayerEntity player) {
        return PLAYER_DISCOVERIES.getOrDefault(player.getUuid(), ImmutableList.of());
    }

    public static void resetDiscoveredRunes(PlayerEntity player) {
        UUID uuid = player.getUuid();
        PLAYER_DISCOVERIES.remove(uuid);
        PLAYER_DISCOVERIES.put(uuid, new ArrayList<>());
    }

    public static void addDiscoveredRune(PlayerEntity player, int rune) {
        UUID uuid = player.getUuid();
        if (!PLAYER_DISCOVERIES.containsKey(uuid))
            PLAYER_DISCOVERIES.put(uuid, new ArrayList<>());

        if (!hasDiscoveredRune(player, rune))
            PLAYER_DISCOVERIES.get(uuid).add(rune);
    }

    public static boolean hasDiscoveredRune(PlayerEntity player, int rune) {
        return getDiscoveredRunes(player).contains(rune);
    }

    public static void syncDiscoveredRunesToClient(ServerPlayerEntity player) {
        int[] discovered = getDiscoveredRunes(player).stream().mapToInt(i -> i).toArray();
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeIntArray(discovered);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Runestones.MSG_CLIENT_SYNC_DISCOVERIES, data);
    }

    public static void populateDiscoveredRunes(PlayerEntity player, int[] discoveries) {
        for (int rune : discoveries) {
            addDiscoveredRune(player, rune);
        }
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
