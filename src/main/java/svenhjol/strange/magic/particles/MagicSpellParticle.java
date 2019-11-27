package svenhjol.strange.magic.particles;

import net.minecraft.client.particle.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.strange.magic.spells.Spell.Element;

@OnlyIn(Dist.CLIENT)
public class MagicSpellParticle extends SpriteTexturedParticle
{
    protected final IAnimatedSprite spriteSet;

    public MagicSpellParticle(World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, IAnimatedSprite sprite)
    {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.spriteSet = sprite;

        if (xSpeed == 0 && zSpeed == 0) {
            this.motionX *= 0.08D;
            this.motionZ *= 0.08D;
        }
        this.motionY *= 0.3D;

        this.particleScale *= 0.5F;
        this.particleGravity = 0.0F;
        this.maxAge = 40;
        this.selectSpriteWithAge(this.spriteSet);
    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            this.selectSpriteWithAge(this.spriteSet);
            this.motionY += 0.004D;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.onGround) {
                this.motionX *= 0.7D;
                this.motionZ *= 0.7D;
            }
        }
    }

    public static class MagicSpellFactory implements IParticleFactory<BasicParticleType>
    {
        protected final IAnimatedSprite spriteSet;

        public MagicSpellFactory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            MagicSpellParticle particle = new MagicSpellParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
            float f = world.rand.nextFloat() * 0.75F + 0.25F;
            float[] color = getElement().getColor();
            particle.setColor(color[0] * f, color[1] * f, color[2] * f);
            return particle;
        }

        public Element getElement()
        {
            return Element.BASE;
        }
    }

    public static class AirSpellFactory extends MagicSpellFactory
    {
        public AirSpellFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Element getElement()
        {
            return Element.AIR;
        }
    }

    public static class WaterSpellFactory extends MagicSpellFactory
    {
        public WaterSpellFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Element getElement()
        {
            return Element.WATER;
        }
    }

    public static class EarthSpellFactory extends MagicSpellFactory
    {
        public EarthSpellFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Element getElement()
        {
            return Element.EARTH;
        }
    }

    public static class FireSpellFactory extends MagicSpellFactory
    {
        public FireSpellFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Element getElement()
        {
            return Element.FIRE;
        }
    }

}
