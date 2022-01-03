package svenhjol.strange.module.mirror_dimension;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.api.event.AddEntityCallback;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.mirror_dimension.network.ServerSendWeatherChange;
import svenhjol.strange.module.mirror_dimension.network.ServerSendWeatherTicks;

import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID, description = "A frightening echo of the overworld with more challenging mobs and weather effects.\n" +
    "By default, the mirror dimension is the only place to find Relics.")
public class MirrorDimension extends CharmModule {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "mirror");
    public static final List<StructureFeature<?>> STRUCTURES_TO_REMOVE;
    public static final List<MobEffect> NEGATIVE_MOB_EFFECTS;
    public static final List<MobEffect> POSITIVE_MOB_EFFECTS;
    public static final List<AmbientParticleSettings> AMBIENT_PARTICLES;

    public static ServerSendWeatherChange SERVER_SEND_WEATHER_CHANGE;
    public static ServerSendWeatherTicks SERVER_SEND_WEATHER_TICKS;

    public static final AmbientParticleSettings ASH;
    public static final AmbientParticleSettings WARPED_SPORE;
    public static final AmbientParticleSettings CRIMSON_SPORE;
    public static final AmbientParticleSettings SPORE_BLOSSOM;

    public static SoundEvent MIRROR_MUSIC;

    public static SoundEvent MIRROR_AMBIENCE_LOOP;
    public static SoundEvent MIRROR_AMBIENCE_ADDITIONS;
    public static AmbientAdditionsSettings MIRROR_AMBIENCE_SETTINGS;

    public static SoundEvent MIRROR_FREEZING_LOOP;
    public static SoundEvent MIRROR_FREEZING_ADDITIONS;
    public static AmbientAdditionsSettings MIRROR_FREEZING_SETTINGS;

    public static SoundEvent MIRROR_SCORCHING_LOOP;
    public static SoundEvent MIRROR_SCORCHING_ADDITIONS;
    public static AmbientAdditionsSettings MIRROR_SCORCHING_SETTINGS;

    public static final ResourceLocation TRIGGER_VISIT_MIRROR = new ResourceLocation(Strange.MOD_ID, "visit_mirror");

    public static final int FOG = 0x004434;
    public static final float TEMP = 0.5F;

    public static final int lightningDistanceFromPlayer = 140; // maximum distance from a random player that a lightning strike can occur
    public static final int minLightningTicks = 200; // minimum number of ticks before another lightning strike occurs

    public static int weatherPhaseTicks = 0;
    public static WeatherPhase weatherPhase = WeatherPhase.STORMING;

    public static int eruptTicks = 0;
    public static int nextEruptAt = 0;
    public static int heatTicks = 0;
    public static int nextHeatAt = 0;
    public static int coldTicks = 0;
    public static int nextColdAt = 0;
    public static int lightningTicks = 0;
    public static int nextLightningAt = 0;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void register() {
        SERVER_SEND_WEATHER_CHANGE = new ServerSendWeatherChange();
        SERVER_SEND_WEATHER_TICKS = new ServerSendWeatherTicks();

        MIRROR_MUSIC = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "mirror_music"));

        MIRROR_AMBIENCE_LOOP = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "mirror_ambience_loop"));
        MIRROR_AMBIENCE_ADDITIONS = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "mirror_ambience_additions"));
        MIRROR_AMBIENCE_SETTINGS = new AmbientAdditionsSettings(MIRROR_AMBIENCE_ADDITIONS, 0.005);

        MIRROR_FREEZING_LOOP = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "mirror_freezing_loop"));
        MIRROR_FREEZING_ADDITIONS = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "mirror_freezing_additions"));
        MIRROR_FREEZING_SETTINGS = new AmbientAdditionsSettings(MIRROR_FREEZING_ADDITIONS, 0.005);

        MIRROR_SCORCHING_LOOP = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "mirror_scorching_loop"));
        MIRROR_SCORCHING_ADDITIONS = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "mirror_scorching_additions"));
        MIRROR_SCORCHING_SETTINGS = new AmbientAdditionsSettings(MIRROR_SCORCHING_ADDITIONS, 0.005);

        Dimensions.SKY_COLOR.put(ID, 0x000000);
        Dimensions.FOG_COLOR.put(ID, FOG);
        Dimensions.GRASS_COLOR.put(ID, 0x607065);
        Dimensions.FOLIAGE_COLOR.put(ID, 0x506055);
        Dimensions.WATER_COLOR.put(ID, 0x525C60);
        Dimensions.WATER_FOG_COLOR.put(ID, 0x404C50);
        Dimensions.PRECIPITATION.put(ID, Biome.Precipitation.SNOW);
        Dimensions.RAIN_LEVEL.put(ID, 0.0F);
        Dimensions.TEMPERATURE.put(ID, TEMP);
        Dimensions.RENDER_PRECIPITATION.put(ID, false);
        Dimensions.AMBIENT_LOOP.put(ID, MIRROR_AMBIENCE_LOOP);
        Dimensions.AMBIENT_ADDITIONS.put(ID, MIRROR_AMBIENCE_SETTINGS);

        addDependencyCheck(m -> Strange.LOADER.isEnabled(Dimensions.class));
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerTickEvents.END_WORLD_TICK.register(this::handleWorldTick);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(this::handlePlayerChangeDimension);
        AddEntityCallback.EVENT.register(this::handleAddEntity);
    }

    public void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        // Remove structures that are not valid in this dimension.
        WorldHelper.removeStructures(level, STRUCTURES_TO_REMOVE);
    }

    public void handlePlayerChangeDimension(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
        if (!DimensionHelper.isDimension(player.level, ID)) return;

        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_VISIT_MIRROR);
        SERVER_SEND_WEATHER_TICKS.send(player, weatherPhase, weatherPhaseTicks);
    }

    public void handleWorldTick(Level level) {
        if (!DimensionHelper.isDimension(level, ID)) return;
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Random random = serverLevel.getRandom();

        handleWeather(serverLevel, random);
    }

    public InteractionResult handleAddEntity(Entity entity) {
        if (!DimensionHelper.isDimension(entity.level, ID)) return InteractionResult.PASS;

        Level level = entity.level;
        if (!level.isClientSide) {
            Random random = new Random(entity.hashCode());
            boolean result;

            if (entity instanceof LivingEntity livingEntity) {
                result = handleMonsterBuffs(livingEntity, random);
                if (!result) return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
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

    private void handleWeather(ServerLevel level, Random random) {
        if (weatherPhaseTicks++ >= weatherPhase.getDuration()) {
            Dimensions.TEMPERATURE.put(ID, TEMP);

            weatherPhaseTicks = 0;
            weatherPhase = WeatherPhase.getNextPhase(weatherPhase);

            SERVER_SEND_WEATHER_CHANGE.send(level.getServer(), weatherPhase);
            LogHelper.debug(Strange.MOD_ID, getClass(), "Setting weather phase to " + weatherPhase);
        }

        switch (weatherPhase) {
            case FREEZING -> handleFreezingPhase(level, random);
            case SCORCHING -> handleScorchingPhase(level, random);
            case STORMING -> handleStormingPhase(level, random);
        }
    }

    private void handleScorchingPhase(ServerLevel level, Random random) {
        float scale = 0;
        int halfScorchTicks = WeatherPhase.SCORCHING.getDuration() / 2;

        if (weatherPhaseTicks >= 0 && weatherPhaseTicks < halfScorchTicks) {
            scale = (float) weatherPhaseTicks / ((float) halfScorchTicks);
        } else if (weatherPhaseTicks >= halfScorchTicks && weatherPhaseTicks < WeatherPhase.FREEZING.getDuration()) {
            scale = 1.0F - (float) (weatherPhaseTicks - halfScorchTicks) / halfScorchTicks;
        }

        if (heatTicks++ > nextHeatAt) {
            heatTicks = 0;
            nextHeatAt = 50 + random.nextInt(150);
            ServerPlayer player = level.getRandomPlayer();
            if (player != null) {
                int range = 20;
                int tries = 5;

                List<Block> meltableBlocks = Arrays.asList(
                    Blocks.SNOW,
                    Blocks.SNOW_BLOCK,
                    Blocks.POWDER_SNOW,
                    Blocks.ICE,
                    Blocks.BLUE_ICE,
                    Blocks.FROSTED_ICE,
                    Blocks.PACKED_ICE
                );

                BlockPos pos = player.blockPosition();
                int px = pos.getX() + random.nextInt(32) - 16;
                int pz = pos.getZ() + random.nextInt(32) - 16;

                List<BlockPos> meltable = new ArrayList<>();
                List<BlockPos> ignitable = new ArrayList<>();

                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                for (int y = surface - (range / 2); y < surface + range; y++) {
                    for (int r = range; r > 7; --r) {
                        for (int t = 1; t < tries; ++t) {
                            BlockPos checkPos = new BlockPos(px + random.nextInt(r), y, pz + random.nextInt(r));
                            Block checkBlock = level.getBlockState(checkPos).getBlock();

                            if (meltableBlocks.contains(checkBlock)) {
                                meltable.add(checkPos);
                            }

                            if (y == surface && ignitable.size() < 4) {
                                ignitable.add(checkPos);
                            }

                            if (meltable.size() > 5) {
                                break;
                            }
                        }
                    }
                }

                if (!ignitable.isEmpty()) {
                    for (BlockPos blockPos : ignitable) {
                        if (BaseFireBlock.canBePlacedAt(level, blockPos, Direction.DOWN)) {
                            BlockState fireState = BaseFireBlock.getState(level, blockPos);
                            level.setBlock(blockPos, fireState, 11);
                        }
                    }
                }

                if (!meltable.isEmpty()) {
                    for (BlockPos blockPos : meltable) {
                        level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        if (eruptTicks++ > nextEruptAt) {
            eruptTicks = 0;
            nextEruptAt = 200 + random.nextInt(300);
            ServerPlayer player = level.getRandomPlayer();
            if (player != null) {
                int range = 14;
                int tries = 20;

                List<Block> replaceableBlocks = Arrays.asList(
                    Blocks.DIRT,
                    Blocks.COARSE_DIRT,
                    Blocks.PODZOL,
                    Blocks.MYCELIUM,
                    Blocks.GRASS_BLOCK,
                    Blocks.GRAVEL,
                    Blocks.DIRT_PATH,
                    Blocks.SAND,
                    Blocks.MAGMA_BLOCK
                );

                BlockPos pos = player.blockPosition();
                int px = pos.getX() + random.nextInt(32) - 16;
                int pz = pos.getZ() + random.nextInt(32) - 16;

                List<BlockPos> replaceable = new ArrayList<>();

                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                for (int y = surface - (range / 2); y < surface + range; y++) {
                    for (int r = range; r > 5; --r) {
                        for (int t = 1; t < tries; ++t) {
                            BlockPos checkPos = new BlockPos(px + random.nextInt(r), y, pz + random.nextInt(r));
                            Block checkBlock = level.getBlockState(checkPos).getBlock();

                            if (replaceableBlocks.contains(checkBlock)) {
                                replaceable.add(checkPos);
                            }

                            if (replaceable.size() > 3) {
                                break;
                            }
                        }
                    }
                }

                if (!replaceable.isEmpty()) {
                    for (BlockPos blockPos : replaceable) {
                        if (level.getBlockState(blockPos).getBlock() == Blocks.MAGMA_BLOCK) {
                            level.setBlockAndUpdate(blockPos, Blocks.LAVA.defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(blockPos, Blocks.MAGMA_BLOCK.defaultBlockState());
                            if (level.getBlockState(blockPos.above()).getBlock() != Blocks.AIR) {
                                level.setBlockAndUpdate(blockPos.above(), Blocks.AIR.defaultBlockState());
                            }
                        }

                        for (int i = 0; i < 5; i++) {
                            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, blockPos.getX() + 0.5F, blockPos.getY() + 0.5F, blockPos.getZ() + 0.5F, random.nextInt(5) + 1, 0.1F, 0.2F, 0.1F, 0.05F);
                        }
                    }
                }
            }
        }

        float newTemp = TEMP + (scale * 0.5F);
        Dimensions.TEMPERATURE.put(ID, newTemp);
    }

    private void handleFreezingPhase(ServerLevel level, Random random) {
        float scale = 0;
        int halfSnowTicks = WeatherPhase.FREEZING.getDuration() / 2;

        if (weatherPhaseTicks >= 0 && weatherPhaseTicks < halfSnowTicks) {
            scale = (float) weatherPhaseTicks / ((float) halfSnowTicks);
            Dimensions.RAIN_LEVEL.put(ID, scale);
        } else if (weatherPhaseTicks >= halfSnowTicks && weatherPhaseTicks < WeatherPhase.FREEZING.getDuration()) {
            scale = 1.0F - (float) (weatherPhaseTicks - halfSnowTicks) / halfSnowTicks;
            Dimensions.RAIN_LEVEL.put(ID, scale);
        }

        if (coldTicks++ > nextColdAt) {
            coldTicks = 0;
            nextColdAt = 50 + random.nextInt(100);
            ServerPlayer player = level.getRandomPlayer();
            if (player != null) {
                int range = 24;
                int tries = 8;

                Map<BlockState, BlockState> replaceableBlocks = new HashMap<>();

                replaceableBlocks.put(Blocks.MAGMA_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState());
                replaceableBlocks.put(Blocks.WATER.defaultBlockState(), Blocks.ICE.defaultBlockState());
                replaceableBlocks.put(Blocks.LAVA.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState());
                replaceableBlocks.put(Blocks.FIRE.defaultBlockState(), Blocks.AIR.defaultBlockState());
                replaceableBlocks.put(Blocks.ICE.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState());
                replaceableBlocks.put(Blocks.CAMPFIRE.defaultBlockState(), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));

                BlockPos pos = player.blockPosition();
                int px = pos.getX() + random.nextInt(16) - 8;
                int pz = pos.getZ() + random.nextInt(16) - 8;

                List<BlockPos> replaceable = new ArrayList<>();

                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                for (int y = surface - (range / 2); y < surface + range; y++) {
                    for (int r = range; r > 0; --r) {
                        for (int t = 1; t < tries; ++t) {
                            BlockPos checkPos = new BlockPos(px + random.nextInt(r), y, pz + random.nextInt(r));
                            BlockState checkState = level.getBlockState(checkPos);

                            if (replaceableBlocks.containsKey(checkState)) {
                                replaceable.add(checkPos);
                            }

                            if (replaceable.size() > 3) {
                                break;
                            }
                        }
                    }
                }

                if (!replaceable.isEmpty()) {
                    for (BlockPos blockPos : replaceable) {
                        BlockState stateAtPos = level.getBlockState(blockPos);
                        BlockState newState = replaceableBlocks.getOrDefault(stateAtPos, null);

                        if (newState != null) {
                            level.setBlockAndUpdate(blockPos, newState);

                            for (int i = 0; i < 5; i++) {
                                level.sendParticles(ParticleTypes.SNOWFLAKE, blockPos.getX() + 0.5F, blockPos.getY() + 0.5F, blockPos.getZ() + 0.5F, random.nextInt(5) + 1, 0.1F, 0.2F, 0.1F, 0.05F);
                            }
                        }
                    }
                }
            }
        }

        float newTemp = TEMP - (scale * 0.5F);
        Dimensions.TEMPERATURE.put(ID, newTemp);
    }

    /** @see ServerLevel#tickChunk */
    private void handleStormingPhase(ServerLevel level, Random random) {
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

            BlockPos lightningPos = level.findLightningTargetAround(new BlockPos(pos.getX() + (dist / 2) - x, pos.getY(), pos.getZ() + (dist / 2) - z));
            if (!level.isLoaded(lightningPos)) return;

            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
            if (lightningBolt == null) return;

            lightningBolt.moveTo(Vec3.atBottomCenterOf(lightningPos));
            lightningBolt.setVisualOnly(false);
            level.addFreshEntity(lightningBolt);
            lightningTicks = 0;
        }
    }

    public enum WeatherPhase {
        FREEZING(10000),
        SCORCHING(8000),
        STORMING(4000),
        CALM(2000);

        private final int duration;

        WeatherPhase(int duration) {
            this.duration = duration;
        }

        public int getDuration() {
            return duration;
        }

        public static WeatherPhase getNextPhase(WeatherPhase current) {
            List<WeatherPhase> phases = Arrays.stream(values()).collect(Collectors.toList());
            phases.remove(current);
            Random random = new Random();
            return phases.get(random.nextInt(phases.size()));
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
        CRIMSON_SPORE = new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.0185F);
        SPORE_BLOSSOM = new AmbientParticleSettings(ParticleTypes.SPORE_BLOSSOM_AIR, 0.0245F);

        AMBIENT_PARTICLES = Arrays.asList(
            WARPED_SPORE, CRIMSON_SPORE, SPORE_BLOSSOM
        );

        STRUCTURES_TO_REMOVE = List.of(
            StructureFeature.RUINED_PORTAL
        );
    }
}
