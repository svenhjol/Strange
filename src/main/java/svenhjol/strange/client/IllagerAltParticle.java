package svenhjol.strange.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Much copypasta from {@link net.minecraft.client.particle.EnchantmentTableParticle}.
 */
@Environment(EnvType.CLIENT)
public class IllagerAltParticle extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    protected IllagerAltParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
        super(clientLevel, d, e, f);
        this.xd = g;
        this.yd = h;
        this.zd = i;
        this.xStart = d;
        this.yStart = e;
        this.zStart = f;
        this.xo = d + g;
        this.yo = e + h;
        this.zo = f + i;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.4f + 0.14f);
        float j = this.random.nextFloat() * 0.6f + 0.4f;
        this.rCol = 0.9f * j;
        this.gCol = 0.9f * j;
        this.bCol = j;
        this.alpha = Math.min(1.0F, random.nextFloat() + 0.3F);
        this.hasPhysics = false;
        this.lifetime = (int)(random.nextDouble() * 60.0) + 10;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    @Override
    public void move(double d, double e, double f) {
        this.setBoundingBox(this.getBoundingBox().move(d, e, f));
        this.setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float f) {
        int i = super.getLightColor(f);
        float g = (float)this.age / (float)this.lifetime;
        g *= g;
        g *= g;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((k += (int)(g * 15.0f * 16.0f)) > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float f = (float)this.age / (float)this.lifetime;
        f = 1.0f - f;
        float g = 1.0f - f;
        g *= g;
        g *= g;
        this.x = this.xStart + this.xd * (double)f;
        this.y = this.yStart + this.yd * (double)f - (double)(g * 1.2f);
        this.z = this.zStart + this.zd * (double)f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
        implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double d, double e, double f, double g, double h, double i) {
            IllagerAltParticle particle = new IllagerAltParticle(level, d, e, f, g, h, i);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
