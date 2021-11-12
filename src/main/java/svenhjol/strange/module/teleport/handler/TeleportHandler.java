package svenhjol.strange.module.teleport.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.runestones.RunestoneHelper;
import svenhjol.strange.module.teleport.EntityTeleportTicket;
import svenhjol.strange.module.teleport.Teleport;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class TeleportHandler<V> {
    protected LivingEntity entity;
    protected ServerLevel level;
    protected ItemStack sacrifice;
    protected String runes;
    protected BlockPos origin;
    protected BlockPos target;
    protected ResourceLocation dimension;
    protected KnowledgeBranch<?, V> branch;
    protected int maxDistance;
    protected V value;

    protected static final List<MobEffect> POSITIVE_EFFECTS;
    protected static final List<MobEffect> NEGATIVE_EFFECTS;

    public TeleportHandler(KnowledgeBranch<?, V> branch, ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos origin) {
        this.branch = branch;
        this.entity = entity;
        this.sacrifice = sacrifice;
        this.runes = runes;
        this.origin = origin;
        this.level = level;
        this.value = branch.get(runes).orElseThrow();
        this.maxDistance = Teleport.maxDistance;
    }

    public abstract void process();

    protected void teleport(boolean exactPosition, boolean allowDimensionChange) {
        if (target == null || dimension == null) {
            return;
        }

        EntityTeleportTicket entry = new EntityTeleportTicket(entity, dimension, origin, target, exactPosition, allowDimensionChange);
        Teleport.entityTickets.add(entry);
    }

    protected boolean checkAndApplyEffects() {
        if (target == null || dimension == null) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1));
            return false;
        }

        List<Item> items = RunestoneHelper.getItems(dimension, runes);
        Item sacrificeItem = sacrifice.getItem();
        Item mainItem = items.get(0);
        Random random = new Random(sacrificeItem.hashCode());

        if (sacrificeItem.equals(mainItem)) {
            int duration = Teleport.protectionDuration * 20;
            int amplifier = 2;
            POSITIVE_EFFECTS.forEach(effect -> entity.addEffect(new MobEffectInstance(effect, duration, amplifier)));
            return true;
        } else if (items.contains(sacrificeItem)) {
            applyNegativeEffect(random);
            return true;
        } else {
            float f = random.nextFloat();
            if (f < 0.1F) {
                level.explode(null, origin.getX(), origin.getY(), origin.getZ(), 2.0F + (random.nextFloat() * 1.5F), Explosion.BlockInteraction.BREAK);
            } else if (f < 0.4F) {
                entity.setSecondsOnFire(5);
            } else if (f < 0.8F) {
                applyNegativeEffect(random);
            } else {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
                    lightning.setVisualOnly(false);
                    level.addFreshEntity(lightning);
                }
            }
        }

        return false;
    }

    private void applyNegativeEffect(Random random) {
        int duration = Teleport.penaltyDuration * 20;
        MobEffect effect = NEGATIVE_EFFECTS.get(random.nextInt(NEGATIVE_EFFECTS.size()));
        entity.addEffect(new MobEffectInstance(effect, duration, 1));
    }

    protected BlockPos checkBounds(Level world, BlockPos pos) {
        WorldBorder border = world.getWorldBorder();

        if (pos.getX() > border.getMaxX())
            pos = new BlockPos(border.getMaxX(), pos.getY(), pos.getZ());

        if (pos.getX() < border.getMinX())
            pos = new BlockPos(border.getMinX(), pos.getY(), pos.getZ());

        if (pos.getZ() > border.getMaxZ())
            pos = new BlockPos(pos.getX(), pos.getY(), border.getMaxZ());

        if (pos.getZ() < border.getMinZ())
            pos = new BlockPos(pos.getX(), pos.getY(), border.getMinZ());

        return pos;
    }

    protected BlockPos getStructureTarget(ResourceLocation id, ServerLevel level, BlockPos origin) {
        Random random = new Random(origin.asLong());
        int xdist = -maxDistance + random.nextInt(maxDistance * 2);
        int zdist = -maxDistance + random.nextInt(maxDistance * 2);
        BlockPos destPos = checkBounds(level, origin.offset(xdist, 0, zdist));
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(id);

        if (structureFeature == null) {
            LogHelper.warn(this.getClass(), "Could not find structure in registry of type: " + id);
            return null;
        }

        LogHelper.debug(this.getClass(), "Trying to locate structure in the world: " + id);
        BlockPos foundPos = level.findNearestMapFeature(structureFeature, destPos, 1000, false);

        if (foundPos == null) {
            LogHelper.warn(this.getClass(), "Could not locate structure: " + id);
            return null;
        }

        return WorldHelper.addRandomOffset(foundPos, random, 6, 12);
    }

    protected BlockPos getBiomeTarget(ResourceLocation id, ServerLevel level, BlockPos origin) {
        Random random = new Random(origin.asLong());
        int xdist = -maxDistance + random.nextInt(maxDistance * 2);
        int zdist = -maxDistance + random.nextInt(maxDistance * 2);
        BlockPos destPos = checkBounds(level, origin.offset(xdist, 0, zdist));

        // TODO check this works with modded biomes
        Optional<Biome> biome = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(id);

        if (biome.isEmpty()) {
            LogHelper.warn(this.getClass(), "Could not find biome in registry of type: " + id);
            return null;
        }

        LogHelper.debug(this.getClass(), "Trying to locate biome in the world: " + id);
        BlockPos foundPos = level.findNearestBiome(biome.get(), destPos, 6400, 8); // ints stolen from LocateBiomeCommand

        if (foundPos == null) {
            LogHelper.warn(this.getClass(), "Could not locate biome: " + id);
            return null;
        }

        return WorldHelper.addRandomOffset(foundPos, random, 6, 12);
    }

    static {
        POSITIVE_EFFECTS = Arrays.asList(MobEffects.SLOW_FALLING, MobEffects.DAMAGE_RESISTANCE, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING);
        NEGATIVE_EFFECTS = Arrays.asList(MobEffects.BLINDNESS, MobEffects.POISON, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.HUNGER, MobEffects.WITHER);
    }
}
