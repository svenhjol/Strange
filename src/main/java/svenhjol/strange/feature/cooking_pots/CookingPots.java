package svenhjol.strange.feature.cooking_pots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import svenhjol.charmony.annotation.Configurable;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.feature.advancements.Advancements;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeTags;

import java.util.List;
import java.util.function.Supplier;

public class CookingPots extends CommonFeature {
    public static final int MAX_PORTIONS = 6;
    static final String BLOCK_ID = "cooking_pot";
    static final String MIXED_STEW_ID = "mixed_stew";
    static Supplier<CookingPotBlock> block;
    static Supplier<CookingPotBlock.BlockItem> blockItem;
    static Supplier<BlockEntityType<CookingPotBlockEntity>> blockEntity;
    static Supplier<MixedStewItem> mixedStewItem;
    static Supplier<SoundEvent> addSound;
    static Supplier<SoundEvent> ambientSound;
    static Supplier<SoundEvent> takeSound;
    static FoodProperties mixedStewFoodProperties;

    @Configurable(
        name = "Hunger restored",
        description = """
            Number of hunger points restored from a single portion of mixed stew."""
    )
    public static int hungerPerStew = 5;

    @Configurable(
        name = "Saturation restored",
        description = """
            Amount of saturation restored from a single portion of mixed stew."""
    )
    public static double saturationPerStew = 0.5f;

    @Configurable(
        name = "Mixed stew stack size",
        description = """
            Maximum stack size of stew obtained from the cooking pot."""
    )
    public static int stewStackSize = 16;

    @Override
    public String description() {
        return """
            Cooking pots allow any food item to be added. Once the combined nourishment total has reached maximum, use wooden bowls to take mixed stew from the pot.
            All negative and positive effects will be removed from the food added to the pot.""";
    }

    @Override
    public void register() {
        var registry = mod().registry();

        block = registry.block(BLOCK_ID, () -> new CookingPotBlock(this));
        blockItem = registry.item(BLOCK_ID, CookingPotBlock.BlockItem::new);
        blockEntity = registry.blockEntity(BLOCK_ID, () -> CookingPotBlockEntity::new, List.of(block));

        mixedStewFoodProperties = new FoodProperties.Builder()
            .nutrition(hungerPerStew)
            .saturationMod((float)saturationPerStew)
            .build();

        mixedStewItem = registry.item(MIXED_STEW_ID, MixedStewItem::new);

        addSound = registry.soundEvent("cooking_pot_add");
        ambientSound = registry.soundEvent("cooking_pot_ambient");
        takeSound = registry.soundEvent("cooking_pot_take");

        CookingPotsNetwork.register(registry);
    }

    public static int getStewStackSize() {
        return Math.max(1, Math.min(64, stewStackSize));
    }

    public static int getMaxPortions() {
        return MAX_PORTIONS;
    }

    public static int getMaxHunger() {
        return hungerPerStew * MAX_PORTIONS;
    }

    public static float getMaxSaturation() {
        return (float)saturationPerStew * MAX_PORTIONS;
    }

    public static boolean isFull(BlockState state) {
        return state.getValue(CookingPotBlock.PORTIONS) == getMaxPortions();
    }

    public static boolean isEmpty(BlockState state) {
        return state.getValue(CookingPotBlock.PORTIONS) <= 0;
    }

    public static boolean isValidHeatSource(BlockState state) {
        var valid = state.is(StrangeTags.COOKING_HEAT_SOURCES);

        if (valid && state.hasProperty(BlockStateProperties.LIT)) {
            return state.getValue(BlockStateProperties.LIT);
        }

        return valid;
    }

    public static void triggerAddedFoodToCookingPot(Player player) {
        Advancements.trigger(new ResourceLocation(Strange.ID, "added_food_to_cooking_pot"), player);
    }

    public static void triggerTookFoodFromCookingPot(Player player) {
        Advancements.trigger(new ResourceLocation(Strange.ID, "took_food_from_cooking_pot"), player);
    }
}
