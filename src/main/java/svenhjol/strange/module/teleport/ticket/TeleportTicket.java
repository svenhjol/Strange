package svenhjol.strange.module.teleport.ticket;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.module.teleport.Teleport;
import svenhjol.strange.module.teleport.iface.ITicket;

import java.util.Random;
import java.util.UUID;

public class TeleportTicket implements ITicket {
    private int ticks;
    private boolean valid;
    private boolean chunkLoaded;
    private boolean success;
    private boolean exactPosition;
    private boolean allowDimensionChange;
    private final LivingEntity entity;
    private final ServerLevel level;
    private final ResourceLocation dimension;
    private final BlockPos from;
    private final BlockPos to;

    public TeleportTicket(LivingEntity entity, ResourceLocation dimension, BlockPos from, BlockPos to) {
        this.entity = entity;
        this.level = (ServerLevel)entity.level;
        this.dimension = dimension;
        this.from = from;
        this.to = to;
        this.exactPosition = false;
        this.allowDimensionChange = false;
        this.valid = true;
        this.chunkLoaded = false;
        this.success = false;
        this.ticks = 0;
    }

    public void useExactPosition(boolean value) {
        this.exactPosition = value;
    }

    public void allowDimensionChange(boolean value) {
        this.allowDimensionChange = value;
    }

    public BlockPos getFrom() {
        return from;
    }

    public BlockPos getTo() {
        return to;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public void tick() {
        boolean sameDimension = DimensionHelper.isDimension(level, dimension);

        if (!sameDimension && !allowDimensionChange) {
            valid = false;
        } else if (sameDimension && !chunkLoaded) {
            chunkLoaded = openChunk();

            if (!chunkLoaded) {
                LogHelper.warn(getClass(), "Could not load destination chunk, giving up");
                valid = false;
                return;
            }

            level.playSound(null, from, StrangeSounds.RUNESTONE_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (!valid) {
            return;
        }

        if (ticks++ >= Teleport.teleportTicks) {
            BlockPos found;
            BlockPos target = to;
            Random r = new Random();

            if (!sameDimension) {
                // dimension change is handled by Fabric API.
                // We create a reposition ticket to safely place the player after teleport.
                MinecraftServer server = level.getServer();
                for (ResourceKey<Level> levelKey : server.levelKeys()) {
                    if (levelKey.location().equals(dimension)) {
                        ServerLevel dest = server.levels.get(levelKey);
                        UUID uuid = entity.getUUID();
                        Teleport.noEndPlatform.remove(uuid);
                        Teleport.noEndPlatform.add(uuid);

                        Vec3 pos = new Vec3(to.getX() + 0.5D, to.getY() + 0.5D, to.getZ() + 0.5D);
                        Vec3 delta = entity.getDeltaMovement();
                        float yRot = entity.getYRot();
                        float xRot = entity.getXRot();
                        PortalInfo portalInfo = new PortalInfo(pos, delta, yRot, xRot);
                        LivingEntity teleportedEntity = FabricDimensions.teleport(entity, dest, portalInfo);

                        if (teleportedEntity == null) {
                            teleportedEntity = entity;
                        }

                        if (!exactPosition) {
                            Teleport.addRepositionTicket(new RepositionTicket(teleportedEntity));
                        }
                        success = true;
                        return;
                    }
                }
            }

            if (!chunkLoaded) {
                valid = false;
                return;
            }

            if (exactPosition) {
                found = target;
                LogHelper.debug(Strange.MOD_ID, getClass(), "Using exactPosition in current dimension: " + found);
            } else {
                if (level.dimensionType().hasCeiling()) {
                    found = WorldHelper.getSurfacePos(level, target, Math.min(level.getSeaLevel() + 40, level.getLogicalHeight() - 20));
                    LogHelper.debug(Strange.MOD_ID, getClass(), "Dimension has ceiling, using limited surfacePos: " + found);
                } else {
                    found = WorldHelper.getSurfacePos(level, target);
                    LogHelper.debug(Strange.MOD_ID, getClass(), "Dimension has no ceiling, using surfacePos: " + found);
                }

                if (found == null) {
                    found = target;
                    LogHelper.debug(Strange.MOD_ID, getClass(), "Could not find a target, using original: " + found);
                }
            }

            entity.teleportToWithTicket(found.getX(), found.getY(), found.getZ());

            if (!exactPosition) {
                // create a reposition ticket to safely land the player after teleport
                Teleport.addRepositionTicket(new RepositionTicket(entity));
            }

            closeChunk();
            success = true;
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    private boolean openChunk() {
        return WorldHelper.addForcedChunk(level, to);
    }

    private void closeChunk() {
        WorldHelper.removeForcedChunk(level, to);
    }
}
