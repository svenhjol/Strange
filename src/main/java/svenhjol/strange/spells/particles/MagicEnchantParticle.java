package svenhjol.strange.spells.particles;

import net.minecraft.client.particle.*;
import net.minecraft.item.DyeColor;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;

public class MagicEnchantParticle extends EnchantmentTableParticle
{
    public MagicEnchantParticle(World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
    {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class MagicEnchantFactory implements IParticleFactory<BasicParticleType>
    {
        protected final IAnimatedSprite spriteSet;

        public MagicEnchantFactory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            MagicEnchantParticle particle = new MagicEnchantParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            float f = world.rand.nextFloat() * 0.75F + 0.25F;
            float[] color = getColor().getColorComponentValues();
            particle.setColor(color[0] * f, color[1] * f, color[2] * f);
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }

        public DyeColor getColor()
        {
            return DyeColor.WHITE;
        }
    }

    public static class WhiteFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public WhiteFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor() { return DyeColor.WHITE; }
    }

    public static class OrangeFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public OrangeFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.ORANGE;
        }
    }

    public static class MagentaFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public MagentaFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.MAGENTA;
        }
    }

    public static class LightBlueFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public LightBlueFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.LIGHT_BLUE;
        }
    }

    public static class YellowFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public YellowFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.YELLOW;
        }
    }

    public static class LimeFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public LimeFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.LIME;
        }
    }

    public static class PinkFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public PinkFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.PINK;
        }
    }

    public static class GrayFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public GrayFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.GRAY;
        }
    }

    public static class LightGrayFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public LightGrayFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.LIGHT_GRAY;
        }
    }

    public static class CyanFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public CyanFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.CYAN;
        }
    }

    public static class PurpleFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public PurpleFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.PURPLE;
        }
    }

    public static class BlueFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public BlueFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.BLUE;
        }
    }

    public static class BrownFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public BrownFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.BROWN;
        }
    }

    public static class GreenFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public GreenFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.GREEN;
        }
    }

    public static class RedFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public RedFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.RED;
        }
    }

    public static class BlackFactory extends MagicSpellParticle.MagicSpellFactory
    {
        public BlackFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public DyeColor getColor()
        {
            return DyeColor.BLACK;
        }
    }
}
