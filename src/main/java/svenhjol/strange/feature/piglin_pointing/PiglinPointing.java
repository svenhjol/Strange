package svenhjol.strange.feature.piglin_pointing;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.StrangeTags;

import java.util.ArrayList;
import java.util.List;

public class PiglinPointing extends CommonFeature {
    public static EntityDataAccessor<Boolean> entityDataIsPointing;
    public static MemoryModuleType<BlockPos> pointingAtTarget;
    static final List<Pair<TagKey<Item>, TagKey<Structure>>> DIRECTION_BARTERING = new ArrayList<>();

    @Override
    public String description() {
        return """
            Piglins turn and point in the rough direction of a nether fortress if given a block of nether bricks,
            or bastions if given a block of polished blackstone bricks.""";
    }

    @Override
    public void register() {
        entityDataIsPointing = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
        pointingAtTarget = MemoryModuleType.register("strange_pointing_at_target");

        registerDirectionBartering(StrangeTags.PIGLIN_BARTERS_FOR_BASTIONS, StrangeTags.PIGLIN_BASTION_LOCATED);
        registerDirectionBartering(StrangeTags.PIGLIN_BARTERS_FOR_FORTRESSES, StrangeTags.PIGLIN_FORTRESS_LOCATED);
    }

    @Override
    public void runWhenEnabled() {
        // Weaken memory types so we can add our custom one in.
        var memoryTypes = new ArrayList<>(Piglin.MEMORY_TYPES);
        memoryTypes.add(pointingAtTarget);
        Piglin.MEMORY_TYPES = ImmutableList.copyOf(memoryTypes);
    }

    public static void registerDirectionBartering(TagKey<Item> items, TagKey<Structure> structure) {
        DIRECTION_BARTERING.add(Pair.of(items, structure));
    }

    /**
     * Called by mixin when piglin has finished considering the block.
     * @see svenhjol.strange.mixin.piglin_pointing.PiglinAiMixin
     */
    public static void checkBlockAndFindStructure(Piglin piglin, ItemStack stack) {
        if (piglin.level() instanceof ServerLevel level) {
            BlockPos source = piglin.blockPosition();
            BlockPos target = null;

            for (var pair : DIRECTION_BARTERING) {
                if (stack.is(pair.getFirst())) {
                    target = level.findNearestMapStructure(pair.getSecond(), source, 500, false);
                }
            }

            if (target != null) {
                piglin.getBrain().setMemoryWithExpiry(PiglinPointing.pointingAtTarget, target, 100L);
            }
        }
    }

    /**
     * Called by mixin to update the piglin entity and model as part of a ticking update check.
     * @see svenhjol.strange.mixin.piglin_pointing.PiglinAiMixin
     */
    public static void setPointing(Piglin piglin) {
        piglin.getBrain().getMemory(PiglinPointing.pointingAtTarget).ifPresentOrElse(
            pos -> {
                piglin.getLookControl().setLookAt(pos.getX(), 60, pos.getZ());
                piglin.getNavigation().stop();
                piglin.getEntityData().set(entityDataIsPointing, true);
            },
            () -> {
                piglin.getEntityData().set(entityDataIsPointing, false);
            }
        );
    }

    /**
     * Called by mixin to check whether a nearby item should be picked up.
     * @see svenhjol.strange.mixin.piglin_pointing.PiglinAiMixin
     */
    public static boolean wantsToPickup(Piglin piglin, ItemStack stack) {
        return !piglin.isBaby()
            && isBarteringItem(stack)
            && isNotAdmiringOrPointing(piglin);
    }

    /**
     * Called by mixin to actually pick up the item and what do to with it.
     * @see svenhjol.strange.mixin.piglin_pointing.PiglinAiMixin
     */
    public static boolean tryToPickup(Piglin piglin, ItemStack stack) {
        if (isBarteringItem(stack)) {
            piglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 80L);
            PiglinAi.holdInOffhand(piglin, stack);
            return true;
        }
        return false;
    }

    public static boolean isNotAdmiringOrPointing(Piglin piglin) {
        return piglin.getBrain().getMemory(MemoryModuleType.ADMIRING_ITEM).isEmpty()
            && piglin.getBrain().getMemory(PiglinPointing.pointingAtTarget).isEmpty();
    }

    public static boolean isPointing(Piglin piglin) {
        return piglin.getEntityData().get(entityDataIsPointing);
    }

    public static boolean isBarteringItem(ItemStack stack) {
        return stack.is(StrangeTags.PIGLIN_BARTERS_FOR_DIRECTIONS);
    }
}
