package svenhjol.strange.module.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.init.StrangeSounds;

import java.util.function.Consumer;

public class TeleportEntry {
    private int ticks;
    private boolean valid;
    private boolean chunkLoaded;
    private boolean success;
    private final boolean exactPosition;
    private final boolean allowDimensionChange;
    private final Consumer<TeleportEntry> onSuccess;
    private final Consumer<TeleportEntry> onFail;
    private final LivingEntity entity;
    private final ResourceLocation dimension;
    private final BlockPos from;
    private final BlockPos to;

    public TeleportEntry(LivingEntity entity, ResourceLocation dimension, BlockPos from, BlockPos to, boolean exactPosition, boolean allowDimensionChange) {
        this(entity, dimension, from, to, exactPosition, allowDimensionChange, t -> {}, t -> {});
    }

    public TeleportEntry(LivingEntity entity, ResourceLocation dimension, BlockPos from, BlockPos to, boolean exactPosition, boolean allowDimensionChange, Consumer<TeleportEntry> onSuccess, Consumer<TeleportEntry> onFail) {
        this.entity = entity;
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
        ServerLevel level = (ServerLevel)entity.level;
        boolean sameDimension = DimensionHelper.isDimension(entity.level, dimension);

        if (!sameDimension) {
            if (allowDimensionChange) {
                // TODO: dimension change
            } else {
                valid = false;
            }
        }

        if (!valid) {
            return;
        }

        if (!chunkLoaded) {
            chunkLoaded = openChunk(level, to);

            if (!chunkLoaded) {
                LogHelper.warn(this.getClass(), "Could not load destination chunk, giving up");
                valid = false;
                return;
            }

            level.playSound(null, from, StrangeSounds.RUNESTONE_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (ticks++ == 20) {
            BlockPos target;

            if (exactPosition) {
                target = WorldHelper.getSurfacePos(level, to);
            } else {
                target = to;
            }

            if (target == null) {
                closeChunk(level, to);
                valid = false;
                return;
            }

            // add protection to the entity that is teleporting
            int duration = 10 * 20;
            entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, duration, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 2));

            entity.teleportToWithTicket(target.getX(), target.getY(), target.getZ());

            closeChunk(level, to);
            success = true;
        }
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isSuccess() {
        return success;
    }

    public void onSuccess() {
        this.onSuccess.accept(this);
    }

    public void onFail() {
        this.onFail.accept(this);
    }

    private boolean openChunk(ServerLevel level, BlockPos pos) {
        return WorldHelper.addForcedChunk(level, to);
    }

    private void closeChunk(ServerLevel level, BlockPos pos) {
        WorldHelper.removeForcedChunk(level, to);
    }
}
