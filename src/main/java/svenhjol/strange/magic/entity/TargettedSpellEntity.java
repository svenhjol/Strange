package svenhjol.strange.magic.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.charm.world.module.EndermitePowder;
import svenhjol.strange.magic.module.SpellBooks;

import java.util.function.Consumer;

public class TargettedSpellEntity extends Entity
{
    public int ticks = 0;

    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;

    private Consumer<RayTraceResult> onImpact = null;

    public TargettedSpellEntity(World world)
    {
        super(SpellBooks.entity, world);
    }

    public TargettedSpellEntity(EntityType<? extends Entity> type, World world)
    {
        super(type, world);
    }

    public TargettedSpellEntity(World world, double x, double y, double z)
    {
        this(EndermitePowder.entity, world);

        this.accelerationX = x * 2.5D;
        this.accelerationY = y * 2.5D;
        this.accelerationZ = z * 2.5D;

        this.setNoGravity(true);
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return new SSpawnObjectPacket(this);
    }

    @Override
    public void tick()
    {
        super.tick();

        double posSpread = 0.5D;
        double scale = 1.25F;
        int maxLiveTime = 100;

        if (ticks++ > maxLiveTime) {
            remove();
            ticks = 0;
        }

        Vec3d vec3d = this.getMotion();

        this.posX += vec3d.x;
        this.posY += vec3d.y;
        this.posZ += vec3d.z;

        ProjectileHelper.rotateTowardsMovement(this, 0.2F);

        double px = posX + (Math.random() - 0.5) * posSpread;
        double py = posY + (Math.random() - 0.5) * posSpread;
        double pz = posZ + (Math.random() - 0.5) * posSpread;
        ((ServerWorld)world).spawnParticle(ParticleTypes.WITCH, px, py, pz, 20,0.0D, 0.0D, 0.0D, 0.6D);

        this.setMotion(vec3d.add(this.accelerationX, this.accelerationY, this.accelerationZ).scale(scale));
        this.setPosition(posX, posY, posZ);

        px = posX + (Math.random() - 0.5) * posSpread;
        py = posY + (Math.random() - 0.5) * posSpread;
        pz = posZ + (Math.random() - 0.5) * posSpread;
        ((ServerWorld)world).spawnParticle(ParticleTypes.WITCH, px, py, pz, 20,0.0D, 0.0D, 0.0D, 0.6D);

        if (this.onImpact != null) {
            RayTraceResult raytraceresult = ProjectileHelper.func_221266_a(this, true, this.ticks >= 1, null, RayTraceContext.BlockMode.COLLIDER);
            if (raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                this.onImpact.accept(raytraceresult);
            }
        }
    }

    public void onImpact(Consumer<RayTraceResult> onImpact)
    {
        this.onImpact = onImpact;
    }

    @Override
    protected void registerData()
    {
        // no op
    }

    @Override
    protected void readAdditional(CompoundNBT compound)
    {
        // no op
    }

    @Override
    protected void writeAdditional(CompoundNBT compound)
    {
        // no op
    }
}
