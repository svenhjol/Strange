package svenhjol.strange.spells.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;

import java.util.function.BiConsumer;

public class TargettedSpellEntity extends Entity
{
    public int ticks = 0;

    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;

    protected LivingEntity caster;
    protected Spell.Element element = Spell.Element.BASE;

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

    public TargettedSpellEntity(World world, LivingEntity caster, double x, double y, double z, Spell.Element element)
    {
        this(Spells.entity, world);
        this.caster = caster;

        this.accelerationX = x * 1.5D;
        this.accelerationY = y * 1.5D;
        this.accelerationZ = z * 1.5D;

        this.setNoGravity(true);
        this.element = element;
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
        BasicParticleType particleType = Spells.spellParticles.get(element);

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
            RayTraceResult result = ProjectileHelper.rayTrace(this, true, false, this.caster, RayTraceContext.BlockMode.COLLIDER);
//                && !(result.getType() == RayTraceResult.Type.BLOCK && world.isAirBlock(((BlockRayTraceResult)result).getPos()))
            if (result.getType() != RayTraceResult.Type.MISS
                && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, result)
            ) {
                this.onImpact.accept(result, this);
            }
        }
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
