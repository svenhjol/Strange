package svenhjol.strange.module.teleport;

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
import svenhjol.strange.init.StrangeSounds;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class EntityTeleportTicket implements ITicket {
    private int ticks;
    private boolean valid;
    private boolean chunkLoaded;
    private boolean success;
    private final boolean exactPosition;
    private final boolean allowDimensionChange;
    private final Consumer<ITicket> onSuccess;
    private final Consumer<ITicket> onFail;
    private final LivingEntity entity;
    private final ServerLevel level;
    private final ResourceLocation dimension;
    private final BlockPos from;
    private final BlockPos to;

    public EntityTeleportTicket(LivingEntity entity, ResourceLocation dimension, BlockPos from, BlockPos to, boolean exactPosition, boolean allowDimensionChange) {
        this(entity, dimension, from, to, exactPosition, allowDimensionChange, t -> {}, t -> {});
    }

    public EntityTeleportTicket(LivingEntity entity, ResourceLocation dimension, BlockPos from, BlockPos to, boolean exactPosition, boolean allowDimensionChange, Consumer<ITicket> onSuccess, Consumer<ITicket> onFail) {
        this.entity = entity;
        this.level = (ServerLevel)entity.level;
        this.dimension = dimension;
        this.from = from;
        this.to = to;
        this.exactPosition = exactPosition;
        this.allowDimensionChange = allowDimensionChange;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
        this.valid = true;
        this.chunkLoaded = false;
        this.success = false;
        this.ticks = 0;
    }

    public void tick() {
        boolean sameDimension = DimensionHelper.isDimension(level, dimension);

        if (!sameDimension && !allowDimensionChange) {
            valid = false;
        } else if (sameDimension && !chunkLoaded) {
            chunkLoaded = openChunk();

            if (!chunkLoaded) {
                LogHelper.warn(this.getClass(), "Could not load destination chunk, giving up");
                valid = false;
                return;
            }

            level.playSound(null, from, StrangeSounds.RUNESTONE_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (!valid) {
            return;
        }

        if (ticks++ == Teleport.TELEPORT_TICKS) {
            BlockPos found;
            BlockPos target = to;
            Random r = new Random();

            if (!sameDimension) {
                // dimension change
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

                        Teleport.repositionTickets.add(new EntityRepositionTicket(teleportedEntity, t -> Teleport.noEndPlatform.remove(uuid)));
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
            } else {
                int tries = 0;
                int maxTries = 5;
                do {
                    if (level.dimensionType().hasCeiling()) {
                        found = WorldHelper.getSurfacePos(level, target, Math.min(level.getSeaLevel() + 40, level.getLogicalHeight() - 20));
                    } else {
                        found = WorldHelper.getSurfacePos(level, target);
                    }
                    if (tries > 0) {
                        int x = to.getX() + (r.nextInt(tries * 2) - tries * 2);
                        int z = to.getZ() + (r.nextInt(tries * 2) - tries * 2);
                        target = new BlockPos(x, 0, z);
                    }
                } while (found == null && tries++ < maxTries);
            }

            if (found == null) {
                closeChunk();
                valid = false;
                return;
            }

            entity.teleportToWithTicket(found.getX(), found.getY(), found.getZ());
            closeChunk();
            success = true;
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public void onSuccess() {
        this.onSuccess.accept(this);
    }

    @Override
    public void onFail() {
        this.onFail.accept(this);
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
