package svenhjol.strange.module.dimensions.mirror;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.Vec3;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.IDimension;

import java.util.*;

public class MirrorDimension implements IDimension {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "mirror");
    public static final List<StructureFeature<?>> STRUCTURES_TO_REMOVE;
    public static final List<MobEffect> NEGATIVE_MOB_EFFECTS;
    public static final List<MobEffect> POSITIVE_MOB_EFFECTS;
    public static final List<AmbientParticleSettings> AMBIENT_PARTICLES;

    public static final AmbientParticleSettings ASH;
    public static final AmbientParticleSettings WARPED_SPORE;
    public static final AmbientParticleSettings SPORE_BLOSSOM;

    public static final int startSnowingAt = 20000; // number of ticks between snowfall
    public static final int maxSnowTicks = 10000; // number of ticks that snowfall will last
    public static final int startParticlesAt = 1000; // number of ticks between particle effects
    public static final int maxParticleTicks = 500; // number of ticks that particle effects will occur
    public static final int lightningDistanceFromPlayer = 140; // maximum distance from a random player that a lightning strike can occur
    public static final int minLightningTicks = 200; // minimum number of ticks before another lightning strike occurs

    public static int snowTicks = 0;
    public static int weatherTicks = 0;
    public static int lightningTicks = 0;
    public static int nextLightningAt = 0;
    public static int particleTicks = 0;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void register() {
        DimensionSpecialEffects.EFFECTS.put(ID, new MirrorEffects());

        Dimensions.SKY_COLOR.put(ID, 0x000000);
        Dimensions.FOG_COLOR.put(ID, 0x004434);
        Dimensions.GRASS_COLOR.put(ID, 0x607065);
        Dimensions.FOLIAGE_COLOR.put(ID, 0x506055);
        Dimensions.WATER_COLOR.put(ID, 0x107070);
        Dimensions.WATER_FOG_COLOR.put(ID, 0x102020);
        Dimensions.PRECIPITATION.put(ID, Biome.Precipitation.SNOW);
        Dimensions.RAIN_LEVEL.put(ID, 0.0F);
        Dimensions.TEMPERATURE.put(ID, 0.0F);
        Dimensions.RENDER_PRECIPITATION.put(ID, false);
        Dimensions.MUSIC.put(ID, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS));
    }

    @Override
    public void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        // Remove structures that are not valid in this dimension.
        removestructures(level, STRUCTURES_TO_REMOVE);
    }

    @Override
    public void handleWorldTick(Level level) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        Random random = serverLevel.getRandom();

        handleSnow();
        handleParticles(random);
        handleLightning(serverLevel, random);
    }

    @Override
    public InteractionResult handleAddEntity(Entity entity) {
        Level level = entity.level;
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel)level;
            Random random = new Random(entity.hashCode());
            boolean result;

            if (entity instanceof LivingEntity livingEntity) {
                result = handleMonsterBuffs(livingEntity, random);
                if (!result) return InteractionResult.FAIL;

                result = handleVillagerRemoval(livingEntity);
                if (!result) return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void handlePlayerTick(Player player) {
        // not yet
    }

    private boolean handleVillagerRemoval(LivingEntity livingEntity) {
        if (livingEntity instanceof Villager villager) {
            villager.discard();
            return false;
        }
        return true;
    }

    private boolean handleMonsterBuffs(LivingEntity livingEntity, Random random) {
        if (random.nextFloat() < 0.6F && livingEntity instanceof Monster && !(livingEntity instanceof Creeper)) {
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
        return true;
    }

    private void handleParticles(Random random) {
        if (weatherTicks++ > startSnowingAt) {
            if (!Dimensions.AMBIENT_PARTICLE.containsKey(ID)) {
                Dimensions.AMBIENT_PARTICLE.put(ID, ASH);
            }
        } else if (particleTicks++ > startParticlesAt) {
            if (!Dimensions.AMBIENT_PARTICLE.containsKey(ID)) {
                Dimensions.AMBIENT_PARTICLE.put(ID, AMBIENT_PARTICLES.get(random.nextInt(AMBIENT_PARTICLES.size())));
            }

            if (particleTicks > startParticlesAt + maxParticleTicks) {
                Dimensions.AMBIENT_PARTICLE.remove(ID);
                particleTicks = 0;
            }
        }
    }

    private void handleSnow() {
        if (weatherTicks++ > startSnowingAt) {
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
    }

    private void handleLightning(ServerLevel level, Random random) {
        if (lightningTicks == 0) {
            nextLightningAt = random.nextInt(400) + minLightningTicks;
        }

        if (lightningTicks++ >= nextLightningAt) {
            ServerPlayer player = level.getRandomPlayer();
            if (player == null) return;
            int dist = lightningDistanceFromPlayer;
            BlockPos pos = player.blockPosition();
            int x = level.random.nextInt(dist);
            int z = level.random.nextInt(dist);

            BlockPos lightningPos = level.findLightningTargetAround(new BlockPos(pos.getX() + (dist/2) - x, pos.getY(), pos.getZ() + (dist/2) - z));
            if (!level.isLoaded(lightningPos)) return;

            /** @see ServerLevel#tickChunk */
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
            if (lightningBolt == null) return;

            lightningBolt.moveTo(Vec3.atBottomCenterOf(lightningPos));
            lightningBolt.setVisualOnly(false);
            level.addFreshEntity(lightningBolt);
            lightningTicks = 0;
        }
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

        ASH = new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.148F);
        WARPED_SPORE = new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.0245F);
        SPORE_BLOSSOM = new AmbientParticleSettings(ParticleTypes.SPORE_BLOSSOM_AIR, 0.0245F);

        AMBIENT_PARTICLES = Arrays.asList(
            WARPED_SPORE, SPORE_BLOSSOM
        );

        STRUCTURES_TO_REMOVE = List.of(
            StructureFeature.RUINED_PORTAL
        );
    }
}
