package svenhjol.strange.module.teleport.runic.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import svenhjol.strange.Strange;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runestones.Runestones;
import svenhjol.strange.module.runestones.helper.RunestoneHelper;
import svenhjol.strange.module.teleport.Teleport;
import svenhjol.strange.module.teleport.runic.RunicTeleportTicket;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class BaseTeleportHandler<V> {
    protected LivingEntity entity;
    protected ServerLevel level;
    protected ItemStack sacrifice;
    protected String runes;
    protected BlockPos originPos;
    protected BlockPos targetPos;
    protected ResourceLocation originDimension;
    protected ResourceLocation targetDimension;
    protected RuneBranch<?, V> branch;
    protected int maxDistance;
    protected V value;

    public static final List<MobEffect> POSITIVE_EFFECTS;
    public static final List<MobEffect> NEGATIVE_EFFECTS;

    public BaseTeleportHandler(RuneBranch<?, V> branch) {
        this.branch = branch;
    }

    public boolean init(ServerLevel level, LivingEntity entity, ItemStack sacrifice, String runes, BlockPos originPos) {
        if (!branch.contains(runes)) {
            return false;
        }

        this.entity = entity;
        this.sacrifice = sacrifice;
        this.runes = runes;
        this.originPos = originPos;
        this.level = level;
        this.value = branch.get(runes);
        this.maxDistance = Runestones.maxDistance;
        this.originDimension = this.entity.level.dimension().location();

        return value != null;
    }

    public abstract boolean process();

    protected boolean teleport(ResourceLocation targetDimension, BlockPos targetPos, boolean exactPosition, boolean allowDimensionChange) {
        this.targetPos = targetPos;
        this.targetDimension = targetDimension;

        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1));

        if (!checkPosAndDimension()) {
            return false;
        }

        ItemOutcome result = checkSacrifice();
        doEffects(result);

        if (result == ItemOutcome.SUCCESS || result == ItemOutcome.PARTIAL_SUCCESS) {
            var ticket = new RunicTeleportTicket(entity, targetDimension, originPos, this.targetPos);
            ticket.useExactPosition(exactPosition);
            ticket.allowDimensionChange(allowDimensionChange);
            Teleport.addTeleportTicket(ticket);
            return true;
        }

        return false;
    }

    protected boolean checkPosAndDimension() {
        return targetPos != null && targetDimension != null;
    }

    protected ItemOutcome checkSacrifice() {
        List<Item> items = RunestoneHelper.getItems(originDimension, runes);
        Item sacrificeItem = sacrifice.getItem();

        if (!items.contains(sacrificeItem)) {
            return ItemOutcome.FAIL;
        }

        if (entity instanceof ServerPlayer player) {
            var journal = Journals.getJournal(player).orElse(null);
            if (journal == null) return ItemOutcome.FAIL;

            var unknownRunes = JournalHelper.countUnknownRunes(runes, journal);
            if (unknownRunes == 0) {
                return ItemOutcome.SUCCESS;
            } else if (unknownRunes < items.size()) {
                return ItemOutcome.PARTIAL_SUCCESS;
            }

            return ItemOutcome.FAIL;
        }

        return ItemOutcome.SUCCESS;
    }

    protected void doEffects(ItemOutcome result) {
        Item sacrificeItem = sacrifice.getItem();
        Random random = new Random(sacrificeItem.hashCode());

        if (result == ItemOutcome.SUCCESS) {
            applyPositiveEffect();
        } else if (result == ItemOutcome.PARTIAL_SUCCESS) {
            applyNegativeEffect(random);
        } else if (result == ItemOutcome.FAIL) {
            applyFailureEffect(random);
        }
    }

    protected void applyPositiveEffect() {
        int duration = Runestones.protectionDuration * 20;
        int amplifier = 2;
        POSITIVE_EFFECTS.forEach(effect -> entity.addEffect(new MobEffectInstance(effect, duration, amplifier)));
    }

    protected void applyNegativeEffect(Random random) {
        int duration = Runestones.penaltyDuration * 20;
        MobEffect effect = NEGATIVE_EFFECTS.get(random.nextInt(NEGATIVE_EFFECTS.size()));
        entity.addEffect(new MobEffectInstance(effect, duration, 1));
    }

    protected void applyFailureEffect(Random random) {
        LogHelper.debug(Strange.MOD_ID, getClass(), "Invalid sacrificial item, doing Bad Things");
        float f = random.nextFloat();
        if (f < 0.05F) {
            WorldHelper.explode(level, originPos, 2.0F + (random.nextFloat() * 1.5F), Explosion.BlockInteraction.BREAK);
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
            LogHelper.warn(getClass(), "Could not find structure in registry of type: " + id);
            return null;
        }

        LogHelper.debug(Strange.MOD_ID, getClass(), "Trying to locate structure in the world: " + id);
        BlockPos foundPos = level.findNearestMapFeature(structureFeature, destPos, 1000, false);

        if (foundPos == null) {
            LogHelper.warn(getClass(), "Could not locate structure: " + id);
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
            LogHelper.warn(getClass(), "Could not find biome in registry of type: " + id);
            return null;
        }

        LogHelper.debug(Strange.MOD_ID, getClass(), "Trying to locate biome in the world: " + id);
        BlockPos foundPos = level.findNearestBiome(biome.get(), destPos, 6400, 8); // ints stolen from LocateBiomeCommand

        if (foundPos == null) {
            LogHelper.warn(getClass(), "Could not locate biome: " + id);
            return null;
        }

        return WorldHelper.addRandomOffset(foundPos, random, 6, 12);
    }

    public enum ItemOutcome {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAIL
    }

    static {
        POSITIVE_EFFECTS = Arrays.asList(MobEffects.SLOW_FALLING, MobEffects.DAMAGE_RESISTANCE, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING);
        NEGATIVE_EFFECTS = Arrays.asList(MobEffects.BLINDNESS, MobEffects.POISON, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.HUNGER, MobEffects.WITHER);
    }
}
