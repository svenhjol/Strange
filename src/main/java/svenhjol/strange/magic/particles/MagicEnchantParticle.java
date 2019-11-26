package svenhjol.strange.magic.particles;

import net.minecraft.client.particle.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;
import svenhjol.strange.magic.spells.Spell;

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
            float[] color = getElement().getColor();
            particle.setColor(color[0] * f, color[1] * f, color[2] * f);
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }

        public Spell.Element getElement()
        {
            return Spell.Element.BASE;
        }
    }

    public static class LightEnchantFactory extends MagicEnchantParticle.MagicEnchantFactory
    {
        public LightEnchantFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Spell.Element getElement()
        {
            return Spell.Element.LIGHT;
        }
    }

    public static class DarkEnchantFactory extends MagicEnchantParticle.MagicEnchantFactory
    {
        public DarkEnchantFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Spell.Element getElement()
        {
            return Spell.Element.DARK;
        }
    }

    public static class AirEnchantFactory extends MagicEnchantParticle.MagicEnchantFactory
    {
        public AirEnchantFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Spell.Element getElement()
        {
            return Spell.Element.AIR;
        }
    }

    public static class WaterEnchantFactory extends MagicEnchantParticle.MagicEnchantFactory
    {
        public WaterEnchantFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Spell.Element getElement()
        {
            return Spell.Element.WATER;
        }
    }

    public static class EarthEnchantFactory extends MagicEnchantParticle.MagicEnchantFactory
    {
        public EarthEnchantFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Spell.Element getElement()
        {
            return Spell.Element.EARTH;
        }
    }

    public static class FireEnchantFactory extends MagicEnchantParticle.MagicEnchantFactory
    {
        public FireEnchantFactory(IAnimatedSprite spriteSet) { super(spriteSet); }

        @Override
        public Spell.Element getElement()
        {
            return Spell.Element.FIRE;
        }
    }
}
