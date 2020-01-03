package svenhjol.strange.spells.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class TargettedSpellEntity extends Entity
{
    public int ticks = 0;

    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;

    protected LivingEntity caster;
    protected Spell spell;

    private BiConsumer<RayTraceResult, TargettedSpellEntity> onImpact = null;

    // Forge complains if this is removed
    public TargettedSpellEntity(World world)
    {
        super(Spells.entity, world);
    }

    public TargettedSpellEntity(EntityType<? extends Entity> type, World world)
    {
        super(type, world);
    }

    public TargettedSpellEntity(World world, LivingEntity caster, double x, double y, double z, Spell spell)
    {
        this(Spells.entity, world);
        this.caster = caster;

        this.accelerationX = x * 1.5D;
        this.accelerationY = y * 1.5D;
        this.accelerationZ = z * 1.5D;

        this.setNoGravity(true);
        this.spell = spell;
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
        double scale = 0.65F;
        int maxLiveTime = 40;

        if (ticks++ > maxLiveTime) {
            remove();
            ticks = 0;
        }

        Vec3d vec3d = this.getMotion();

        this.posX += vec3d.x;
        this.posY += vec3d.y;
        this.posZ += vec3d.z;

//        ProjectileHelper.rotateTowardsMovement(this, 0.2F);
        BasicParticleType particleType = Spells.spellParticle;

        double px = posX + (Math.random() - 0.5) * posSpread;
        double py = posY + (Math.random() - 0.5) * posSpread;
        double pz = posZ + (Math.random() - 0.5) * posSpread;
        ((ServerWorld)world).spawnParticle(particleType, px, py, pz, 10,0.0D, 0.0D, 0.0D, 0.6D);

        this.setMotion(vec3d.add(this.accelerationX, this.accelerationY, this.accelerationZ).scale(scale));
        this.setPosition(posX, posY, posZ);

        px = posX + (Math.random() - 0.5) * posSpread;
        py = posY + (Math.random() - 0.5) * posSpread;
        pz = posZ + (Math.random() - 0.5) * posSpread;
        ((ServerWorld)world).spawnParticle(particleType, px, py, pz, 10,0.0D, 0.0D, 0.0D, 0.6D);

        if (this.onImpact != null && ticks > 1) {

            Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d vec3d2 = vec3d1.add(vec3d);
            RayTraceResult r = this.world.rayTraceBlocks(new RayTraceContext(vec3d1, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (r.getType() != RayTraceResult.Type.MISS) {
                vec3d2 = r.getHitVec();
            }

            EntityRayTraceResult er = this.rayTraceEntities(vec3d1, vec3d2);
            if (er != null) {
                r = er;
            }

            if (r.getType() != RayTraceResult.Type.MISS
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, r)
            ) {
                this.onImpact.accept(r, this);
            }
        }
    }

    @Nullable
    protected EntityRayTraceResult rayTraceEntities(Vec3d startVec, Vec3d endVec) {
        return ProjectileHelper.rayTraceEntities(this.world, this, startVec, endVec, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (entity) -> {
            return !entity.isSpectator() && entity.isAlive() && entity.canBeCollidedWith() && (entity != this.caster || this.ticks >= 5);
        });
    }

    public void onImpact(BiConsumer<RayTraceResult, TargettedSpellEntity> onImpact)
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
