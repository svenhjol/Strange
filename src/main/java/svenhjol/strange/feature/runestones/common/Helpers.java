package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.charmony.Resolve;
import svenhjol.charm.charmony.common.helper.TagHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.api.enums.RunestoneLocationType;
import svenhjol.strange.api.impl.RunestoneLocation;
import svenhjol.strange.feature.runestones.Runestones;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public final class Helpers {
    public static final Runestones RUNESTONES = Resolve.feature(Runestones.class);
    public static final ResourceLocation SPAWN_POINT_ID = Strange.id("spawn_point");
    public static final RunestoneLocation SPAWN_POINT = new RunestoneLocation(RunestoneLocationType.PLAYER, SPAWN_POINT_ID);

    /**
     * Make a random runestone biome location from a given biome tag.
     */
    public static Optional<RunestoneLocation> randomBiome(LevelAccessor level, TagKey<Biome> tag, RandomSource random) {
        var values = getValues(level.registryAccess(), tag);
        if (!values.isEmpty()) {
            var location = new RunestoneLocation(RunestoneLocationType.BIOME, values.get(random.nextInt(values.size())));
            return Optional.of(location);
        }

        return Optional.empty();
    }

    /**
     * Make a random runestone structure location from a given structure tag.
     */
    public static Optional<RunestoneLocation> randomStructure(LevelAccessor level, TagKey<Structure> tag, RandomSource random) {
        var values = getValues(level.registryAccess(), tag);
        if (!values.isEmpty()) {
            var location = new RunestoneLocation(RunestoneLocationType.STRUCTURE, values.get(random.nextInt(values.size())));
            return Optional.of(location);
        }

        return Optional.empty();
    }

    /**
     * Get a random runestone sacrifice item from a given item tag.
     */
    public static Optional<Item> randomItem(LevelAccessor level, TagKey<Item> tag, RandomSource random) {
        var values = TagHelper.getValues(level.registryAccess().registryOrThrow(Registries.ITEM), tag);
        if (!values.isEmpty()) {
            return Optional.of(values.get(random.nextInt(values.size())));
        }
        return Optional.empty();
    }

    public static BlockPos addRandomOffset(Level level, BlockPos pos, RandomSource random, int min, int max) {
        var n = random.nextInt(max - min) + min;
        var e = random.nextInt(max - min) + min;
        var s = random.nextInt(max - min) + min;
        var w = random.nextInt(max - min) + min;

        pos = pos.north(random.nextBoolean() ? n : -n);
        pos = pos.east(random.nextBoolean() ? e : -e);
        pos = pos.south(random.nextBoolean() ? s : -s);
        pos = pos.west(random.nextBoolean() ? w : -w);

        // World border checking
        var border = level.getWorldBorder();
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        if (x < border.getMinX()) {
            pos = new BlockPos((int)border.getMinX(), y, z);
        } else if (x > border.getMaxX()) {
            pos = new BlockPos((int)border.getMaxX(), y, z);
        }
        if (z < border.getMinZ()) {
            pos = new BlockPos(x, y, (int)border.getMinZ());
        } else if (z > border.getMaxZ()) {
            pos = new BlockPos(x, y, (int)border.getMaxZ());
        }

        return pos;
    }

    /**
     * Get all values of a given tag.
     */
    public static <T> List<ResourceLocation> getValues(RegistryAccess registryAccess, TagKey<T> tag) {
        var registry = registryAccess.registryOrThrow(tag.registry());
        return TagHelper.getValues(registry, tag)
            .stream().map(registry::getKey).toList();
    }

    /**
     * @see ServerPlayer#changeDimension
     * TODO: move to charmony
     */
    public static void changeDimension(ServerPlayer serverPlayer, ServerLevel newDimension, Vec3 pos) {
        serverPlayer.isChangingDimension = true;
        var connection = serverPlayer.connection;
        var currentDimension = serverPlayer.serverLevel();
        var levelData = newDimension.getLevelData();

        connection.send(new ClientboundRespawnPacket(serverPlayer.createCommonSpawnInfo(newDimension), (byte)3));
        connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));

        var playerList = currentDimension.getServer().getPlayerList();
        playerList.sendPlayerPermissionLevel(serverPlayer);
        currentDimension.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
        serverPlayer.unsetRemoved();

        var yRot = serverPlayer.getYRot();
        var xRot = serverPlayer.getXRot();

        serverPlayer.setServerLevel(newDimension);
        connection.teleport(pos.x(), pos.y(), pos.z(), yRot, xRot);
        connection.resetPosition();

        newDimension.addDuringTeleport(serverPlayer);

        connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));
        connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));

        playerList.sendLevelInfo(serverPlayer, newDimension);
        playerList.sendAllPlayerInfo(serverPlayer);
        playerList.sendActivePlayerEffects(serverPlayer);
        
        serverPlayer.lastSentExp = -1;
        serverPlayer.lastSentHealth = -1.0f;
        serverPlayer.lastSentFood = -1;
    }

    @Nullable
    public static BlockPos getSurfacePos(Level level, BlockPos pos, int startAtHeight) {
        int surface = 0;

        for (int y = startAtHeight; y >= 0; --y) {
            BlockPos n = new BlockPos(pos.getX(), y, pos.getZ());
            if (level.isEmptyBlock(n)
                && !level.isEmptyBlock(n.below())
                && !level.getBlockState(n.below()).is(Blocks.LAVA)) {
                surface = y;
                break;
            }
        }

        if (surface == 0) {
            RUNESTONES.log().warn("Failed to find a surface value to spawn the player");
            return null;
        }

        return new BlockPos(pos.getX(), surface, pos.getZ());
    }
}
