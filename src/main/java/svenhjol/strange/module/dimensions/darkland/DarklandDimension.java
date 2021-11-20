package svenhjol.strange.module.dimensions.darkland;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.IDimension;

import java.util.*;

public class DarklandDimension implements IDimension {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "darkland");
    public static final List<MobEffect> NEGATIVE_MOB_EFFECTS;
    public static final List<MobEffect> POSITIVE_MOB_EFFECTS;
    public static int snowTicks = 0;
    public static int weatherTicks = 0;
    public static int lightningTicks = 0;
    public static int nextLightningAt = 0;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void register() {
        Dimensions.SKY_COLOR.put(ID, 0x000000);
        Dimensions.FOG_COLOR.put(ID, 0x004434);
        Dimensions.GRASS_COLOR.put(ID, 0x607265);
        Dimensions.FOLIAGE_COLOR.put(ID, 0x607265);
        Dimensions.WATER_COLOR.put(ID, 0x102020);
        Dimensions.WATER_FOG_COLOR.put(ID, 0x102020);
        Dimensions.PRECIPITATION.put(ID, Biome.Precipitation.SNOW);
        Dimensions.RAIN_LEVEL.put(ID, 0.0F);
        Dimensions.TEMPERATURE.put(ID, 0.0F);
        Dimensions.RENDER_PRECIPITATION.put(ID, false);
        Dimensions.AMBIENT_PARTICLE.put(ID, new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F));
        Dimensions.MUSIC.put(ID, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS));
    }

    @Override
    public void handleWorldTick(Level level) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        Random random = serverLevel.getRandom();

        int startSnowingAt = 20000;

        if (weatherTicks++ > startSnowingAt) {
            int maxSnowTicks = 10000;
            int halfSnowTicks = maxSnowTicks / 2;

            if (snowTicks >= 0 && snowTicks < halfSnowTicks) {
                float rainLevel = (float) snowTicks / ((float) halfSnowTicks);
                Dimensions.RAIN_LEVEL.put(ID, rainLevel);
            } else if (snowTicks >= halfSnowTicks && snowTicks < maxSnowTicks) {
                float rainLevel = 1.0F - (float) (snowTicks - halfSnowTicks) / halfSnowTicks;
                Dimensions.RAIN_LEVEL.put(ID, rainLevel);
            } else if (snowTicks >= maxSnowTicks) {
                snowTicks = 0;
                weatherTicks = 0;
            }

            snowTicks++;
        }

        if (lightningTicks == 0) {
            nextLightningAt = random.nextInt(400) + 200;
        }

        if (lightningTicks++ >= nextLightningAt) {
            ServerPlayer player = ((ServerLevel) level).getRandomPlayer();
            if (player == null) return;

            int dist = 140;
            BlockPos pos = player.blockPosition();
            int x = level.random.nextInt(dist);
            int z = level.random.nextInt(dist);
            BlockPos lightningPos = serverLevel.findLightningTargetAround(new BlockPos(pos.getX() + (dist/2) - x, pos.getY(), pos.getZ() + (dist/2) - z));
            if (!level.isLoaded(lightningPos)) return;

            /** @see ServerLevel#tickChunk */
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightningBolt == null) return;

            lightningBolt.moveTo(Vec3.atBottomCenterOf(lightningPos));
            lightningBolt.setVisualOnly(false);
            serverLevel.addFreshEntity(lightningBolt);
            lightningTicks = 0;
        }
    }

    @Override
    public void handleAddEntity(Entity entity) {
        Level level = entity.level;
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Random random = new Random(entity.hashCode());

            if (entity instanceof LivingEntity livingEntity) {
                // passive mobs should be unwell
                if (random.nextFloat() < 0.75F && livingEntity instanceof Animal && !(livingEntity instanceof NeutralMob)) {
                    MobEffect mobEffect = NEGATIVE_MOB_EFFECTS.get(random.nextInt(NEGATIVE_MOB_EFFECTS.size()));
                    int duration = 3600;
                    int amplifier = 1;
                    livingEntity.addEffect(new MobEffectInstance(mobEffect, duration, amplifier));
                }

                // monsters have buffs
                if (random.nextFloat() < 0.6F && livingEntity instanceof Monster) {
                    int duration = 3600;
                    int amplifier = random.nextInt(3) + 1;
                    List<MobEffect> mobEffects = new ArrayList<>(POSITIVE_MOB_EFFECTS);
                    Collections.shuffle(mobEffects, random);
                    List<MobEffect> toApply;

                    // try increase health
                    AttributeMap attributes = livingEntity.getAttributes();
                    AttributeInstance healthInstance = attributes.getInstance(Attributes.MAX_HEALTH);

                    if (healthInstance != null) {
                        double defaultValue = healthInstance.getBaseValue();
                        double newValue = defaultValue * (1 + random.nextFloat());
                        healthInstance.setBaseValue(newValue);
                        livingEntity.setHealth((float)newValue);
                    }

                    // chance of applying more than one buff
                    if (random.nextFloat() < 0.25F) {
                        toApply = mobEffects.subList(0, random.nextInt(Math.min(3, mobEffects.size() - 1)) + 1);
                    } else {
                        toApply = List.of(mobEffects.get(0));
                    }

                    toApply.forEach(e -> livingEntity.addEffect(new MobEffectInstance(e, duration, amplifier)));
                }
            }
        }
    }

    @Override
    public void handlePlayerTick(Player player) {
        // not yet
    }

    static {
        NEGATIVE_MOB_EFFECTS = Arrays.asList(
            MobEffects.POISON,
            MobEffects.WITHER
        );

        POSITIVE_MOB_EFFECTS = Arrays.asList(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.REGENERATION,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.DAMAGE_BOOST,
            MobEffects.HEALTH_BOOST,
            MobEffects.INVISIBILITY
        );
    }
}
