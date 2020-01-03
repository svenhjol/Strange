package svenhjol.strange.spells.particles;

import net.minecraft.client.particle.*;
import net.minecraft.item.DyeColor;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
            float[] color = getColor().getColorComponentValues();
            particle.setColor(color[0] * f, color[1] * f, color[2] * f);
            return particle;
        }

        public DyeColor getColor()
        {
            return DyeColor.WHITE;
        }
    }

    public static class WhiteFactory extends MagicSpellFactory
    {
        public WhiteFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor() { return DyeColor.WHITE; }
    }

    public static class OrangeFactory extends MagicSpellFactory
    {
        public OrangeFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.ORANGE;
        }
    }

    public static class MagentaFactory extends MagicSpellFactory
    {
        public MagentaFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.MAGENTA;
        }
    }

    public static class LightBlueFactory extends MagicSpellFactory
    {
        public LightBlueFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.LIGHT_BLUE;
        }
    }

    public static class YellowFactory extends MagicSpellFactory
    {
        public YellowFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.YELLOW;
        }
    }

    public static class LimeFactory extends MagicSpellFactory
    {
        public LimeFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.LIME;
        }
    }

    public static class PinkFactory extends MagicSpellFactory
    {
        public PinkFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.PINK;
        }
    }

    public static class GrayFactory extends MagicSpellFactory
    {
        public GrayFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.GRAY;
        }
    }

    public static class LightGrayFactory extends MagicSpellFactory
    {
        public LightGrayFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.LIGHT_GRAY;
        }
    }

    public static class CyanFactory extends MagicSpellFactory
    {
        public CyanFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.CYAN;
        }
    }

    public static class PurpleFactory extends MagicSpellFactory
    {
        public PurpleFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.PURPLE;
        }
    }

    public static class BlueFactory extends MagicSpellFactory
    {
        public BlueFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.BLUE;
        }
    }

    public static class BrownFactory extends MagicSpellFactory
    {
        public BrownFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.BROWN;
        }
    }

    public static class GreenFactory extends MagicSpellFactory
    {
        public GreenFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.GREEN;
        }
    }

    public static class RedFactory extends MagicSpellFactory
    {
        public RedFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.RED;
        }
    }

    public static class BlackFactory extends MagicSpellFactory
    {
        public BlackFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.BLACK;
        }
    }
}
