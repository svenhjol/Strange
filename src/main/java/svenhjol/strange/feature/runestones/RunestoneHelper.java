package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.*;

public class RunestoneHelper {
    static final char FIRST_RUNE = 'a';
    static final char LAST_RUNE = 'z';
    static final int NUM_RUNES = 26;
    static final Map<String, String> CACHED_RUNIC_NAMES = new WeakHashMap<>();
    public static final ResourceLocation SPAWN_POINT_ID = new ResourceLocation(Strange.ID, "spawn_point");
    public static final Location SPAWN_POINT_LOCATION = new Location(LocationType.PLAYER, SPAWN_POINT_ID);

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

    public static BlockPos getBlockLookedAt(Player player) {
        var cameraPosVec = player.getEyePosition(1.0F);
        var rotationVec = player.getViewVector(1.0F);
        var vec3d = cameraPosVec.add(rotationVec.x * 6, rotationVec.y * 6, rotationVec.z * 6);
        var raycast = player.level().clip(new ClipContext(cameraPosVec, vec3d, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return raycast.getBlockPos();
    }

    public static String getLocaleKey(Location location) {
        var namespace = location.id().getNamespace();
        var path = location.id().getPath();

        return switch (location.type()) {
            case BIOME -> "biome." + namespace + "." + path;
            case STRUCTURE -> "structure." + namespace + "." + path;
            case PLAYER -> "player." + namespace + "." + path;
        };
    }

    public static String getRunicName(Location location) {
        if (!RunestonesClient.hasReceivedSeed) {
            throw new RuntimeException("Client has not received the level seed");
        }

        var combined = location.type().name().substring(0, 2) + location.id().getPath();

        if (!CACHED_RUNIC_NAMES.containsKey(combined)) {
            var random = RandomSource.create(RunestonesClient.seed);
            CACHED_RUNIC_NAMES.put(combined, generateRunes(combined, 16, random));
        }

        return CACHED_RUNIC_NAMES.get(combined);
    }

    /**
     * Generate runes for a given input string. The string is filtered to make it alphanumeric.
     * Each character of the string is shifted through the alphabet randomly.
     */
    public static String generateRunes(String input, int length, RandomSource random) {
        int alphaStart = FIRST_RUNE;
        int alphaEnd = LAST_RUNE;

        String filtered = input.replaceAll("[^a-zA-Z0-9]", "");
        StringBuilder in = new StringBuilder(filtered);
        StringBuilder out = new StringBuilder();

        for (int tries = 0; tries < 9; tries++) {
            if (in.length() >= length) {
                random.nextInt();
                char[] chars = in.toString().toLowerCase(Locale.ROOT).toCharArray();

                // Work over the string backwards by character.
                for (int i = Math.min(chars.length, length) - 1; i >= 0; --i) {
                    int chr = chars[i];

                    if (chr >= alphaStart && chr <= alphaEnd) {
                        // Shift the char with a random number of the total runes, wrapping around if it goes out of bounds.
                        int ri = chr + random.nextInt(NUM_RUNES);
                        if (ri > alphaEnd) {
                            chr = Mth.clamp(alphaStart + (ri - alphaEnd), alphaStart + 1, alphaEnd);
                        }

                        // Shift the char again with a random number of half the total runes, wrapping again as necessary.
                        ri += random.nextInt(NUM_RUNES / 2);
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

        throw new RuntimeException("Maximum loops reached when checking string length");
    }

    @Nullable
    public static BlockPos getSurfacePos(Level level, BlockPos pos) {
        return getSurfacePos(level, pos, level.getMaxBuildHeight());
    }

    @Nullable
    public static BlockPos getSurfacePos(Level level, BlockPos pos, int startAtHeight) {
        var log = Mods.common(Strange.ID).log();
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
            log.warn(RunestoneHelper.class, "Failed to find a surface value to spawn the player");
            return null;
        }

        return new BlockPos(pos.getX(), surface, pos.getZ());
    }

    public static ResourceLocation getDimensionId(Level level) {
        return level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getKey(level.dimensionType());
    }

    public static ResourceLocation getDimensionId(ResourceKey<Level> key) {
        return key.location();
    }

    /**
     * Helper method to get all values of a given tag.
     */
    public static <T> List<ResourceLocation> getValues(RegistryAccess registryAccess, TagKey<T> tag) {
        var registry = registryAccess.registryOrThrow(tag.registry());
        return TagHelper.getValues(registry, tag)
            .stream().map(registry::getKey).toList();
    }

    /**
     * Helper method to get a location from a given tag.
     */
    public static <T> Optional<Location> getRandomLocation(RegistryAccess registryAccess, TagKey<T> tag, LocationType type, RandomSource random) {
        var values = getValues(registryAccess, tag);
        if (!values.isEmpty()) {
            var location = new Location(type, values.get(random.nextInt(values.size())));
            return Optional.of(location);
        }

        return Optional.empty();
    }

    public static void changeDimension(ServerPlayer serverPlayer, ServerLevel newDimension, Vec3 pos) {
        serverPlayer.isChangingDimension = true;
        var currentDimension = serverPlayer.serverLevel();
        var levelData = newDimension.getLevelData();

        serverPlayer.connection.send(new ClientboundRespawnPacket(serverPlayer.createCommonSpawnInfo(newDimension), (byte)3));
        serverPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));

        var playerList = currentDimension.getServer().getPlayerList();
        playerList.sendPlayerPermissionLevel(serverPlayer);
        currentDimension.removePlayerImmediately(serverPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
        serverPlayer.unsetRemoved();

        var delta = serverPlayer.getDeltaMovement();
        var yRot = serverPlayer.getYRot();
        var xRot = serverPlayer.getXRot();
        var portalInfo = new PortalInfo(pos, delta, yRot, xRot);

        serverPlayer.setServerLevel(newDimension);
        serverPlayer.connection.teleport(portalInfo.pos.x, portalInfo.pos.y, portalInfo.pos.z, portalInfo.yRot, portalInfo.xRot);
        serverPlayer.connection.resetPosition();

        newDimension.addDuringPortalTeleport(serverPlayer);

        serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));
        playerList.sendLevelInfo(serverPlayer, newDimension);
        playerList.sendAllPlayerInfo(serverPlayer);

        for (MobEffectInstance effect : serverPlayer.getActiveEffects()) {
            serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), effect));
        }

        serverPlayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
        serverPlayer.lastSentExp = -1;
        serverPlayer.lastSentHealth = -1.0f;
        serverPlayer.lastSentFood = -1;
    }

    public static void addForcedChunk(ServerLevel level, Vec3 target) {
        var log = Mods.common(Strange.ID).log();
        var blockPos = new BlockPos((int) target.x, (int) target.y, (int) target.z);
        var chunkPos = new ChunkPos(blockPos);

        // try a couple of times I guess?
        var result = false;
        for (int i = 0; i <= 2; i++) {
            result = level.setChunkForced(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), true);
            if (result) break;
        }

        if (result) {
            log.debug(RunestoneHelper.class, "Force loaded chunk " + chunkPos);
        }
    }

    public void removeForcedChunk(ServerLevel level, Vec3 target) {
        var log = Mods.common(Strange.ID).log();
        var blockPos = new BlockPos((int) target.x, (int) target.y, (int) target.z);
        var chunkPos = new ChunkPos(blockPos);

        var result = level.setChunkForced(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), false);
        if (!result) {
            log.debug(RunestoneHelper.class, "Forced chunk was already removed.");
        } else {
            log.debug(RunestoneHelper.class, "Unloaded forced chunk " + chunkPos);
        }
    }
}
