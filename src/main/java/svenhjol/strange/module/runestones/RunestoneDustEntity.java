package svenhjol.strange.module.runestones;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RunestoneDustEntity extends Entity {
    public int ticks = 0;

    private static final EntityDataAccessor<Integer> TARGET_X = SynchedEntityData.defineId(RunestoneDustEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_Z = SynchedEntityData.defineId(RunestoneDustEntity.class, EntityDataSerializers.INT);
    private static final String TARGET_X_NBT = "targetX";
    private static final String TARGET_Z_NBT = "targetZ";

    public RunestoneDustEntity(EntityType<? extends RunestoneDustEntity> type, Level world) {
        super(type, world);
    }

    public RunestoneDustEntity(Level world, int x, int z) {
        this(Runestones.RUNESTONE_DUST_ENTITY, world);
        entityData.set(TARGET_X, x);
        entityData.set(TARGET_Z, z);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TARGET_X, 0);
        entityData.define(TARGET_Z, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(TARGET_X, tag.getInt(TARGET_X_NBT));
        entityData.set(TARGET_Z, tag.getInt(TARGET_Z_NBT));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(TARGET_X_NBT, entityData.get(TARGET_X));
        tag.putInt(TARGET_Z_NBT, entityData.get(TARGET_Z));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void tick() {
        super.tick();

        double posSpread = 0.5D;
        double scale = 0.2D;
        double rise = 0.2D;
        double speed = 0.5D;

        int maxLiveTime = 130;
        int particles = 15;

        int x = blockPosition().getX();
        int y = blockPosition().getY();
        int z = blockPosition().getZ();

        Vec3 vec = new Vec3((double) entityData.get(TARGET_X), y, (double) entityData.get(TARGET_Z))
            .subtract(x, y, z).normalize().scale(scale);

        double bpx = x + vec.x * ticks;
        double bpy = y + vec.y * ticks + ticks * ((((float)(maxLiveTime - ticks) / maxLiveTime) * rise) + rise);
        double bpz = z + vec.z * ticks;

        if (!level.isClientSide) {
            for (int i = 0; i < particles; i++) {
                double px = bpx + (Math.random() - 0.5) * posSpread;
                double py = bpy + (Math.random() - 0.5) * posSpread;
                double pz = bpz + (Math.random() - 0.5) * posSpread;
                ((ServerLevel) level).sendParticles(ParticleTypes.ASH, px, py, pz, 1, 0.2D, 0.12D, 0.1D, speed);
            }
        }

        if (ticks++ > maxLiveTime) {
            discard();
            ticks = 0;
        }
    }
}
