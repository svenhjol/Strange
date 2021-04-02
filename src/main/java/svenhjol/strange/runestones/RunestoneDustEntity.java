package svenhjol.strange.runestones;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("EntityConstructor")
public class RunestoneDustEntity extends Entity {
    public int ticks = 0;

    private static final TrackedData<Integer> TARGET_X = DataTracker.registerData(RunestoneDustEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TARGET_Z = DataTracker.registerData(RunestoneDustEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final String TAG_TARGET_X = "targetX";
    private static final String TAG_TARGET_Z = "targetZ";

    public RunestoneDustEntity(EntityType<? extends RunestoneDustEntity> type, World world) {
        super(type, world);
    }

    public RunestoneDustEntity(World world, int x, int z) {
        this(Runestones.RUNESTONE_DUST_ENTITY, world);
        dataTracker.set(TARGET_X, x);
        dataTracker.set(TARGET_Z, z);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(TARGET_X, 0);
        dataTracker.startTracking(TARGET_Z, 0);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
        dataTracker.set(TARGET_X, tag.getInt(TAG_TARGET_X));
        dataTracker.set(TARGET_Z, tag.getInt(TAG_TARGET_Z));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
        tag.putInt(TAG_TARGET_X, dataTracker.get(TARGET_X));
        tag.putInt(TAG_TARGET_Z, dataTracker.get(TARGET_Z));
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
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

        int x = getBlockPos().getX();
        int y = getBlockPos().getY();
        int z = getBlockPos().getZ();

        Vec3d vec = new Vec3d((double) dataTracker.get(TARGET_X), y, (double) dataTracker.get(TARGET_Z))
            .subtract(x, y, z).normalize().multiply(scale);

        double bpx = x + vec.x * ticks;
        double bpy = y + vec.y * ticks + ticks * ((((float)(maxLiveTime - ticks) / maxLiveTime) * rise) + rise);
        double bpz = z + vec.z * ticks;

        if (!world.isClient) {
            for (int i = 0; i < particles; i++) {
                double px = bpx + (Math.random() - 0.5) * posSpread;
                double py = bpy + (Math.random() - 0.5) * posSpread;
                double pz = bpz + (Math.random() - 0.5) * posSpread;
                ((ServerWorld) world).spawnParticles(ParticleTypes.ASH, px, py, pz, 1, 0.2D, 0.12D, 0.1D, speed);
            }
        }

        if (ticks++ > maxLiveTime) {
            discard();
            ticks = 0;
        }
    }
}
