package svenhjol.strange.feature.cooking_pots;

import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charmony.annotation.Configurable;
import svenhjol.charmony.common.CommonFeature;

import java.util.function.Supplier;

public class CookingPots extends CommonFeature {
    static Supplier<CookingPotBlock> block;
    static Supplier<BlockEntityType<CookingPotBlockEntity>> blockEntity;

    @Configurable(
        name = "Hunger restored",
        description = """
            Number of hunger points restored from a single portion of mixed stew."""
    )
    public static int hungerPerStew = 6;

    @Configurable(
        name = "Saturation restored",
        description = """
            Amount of saturation restored from a single portion of mixed stew."""
    )
    public static double saturationPerStew = 4.0;

    @Configurable(
        name = "Maximum portions",
        description = """
            Number of stew portions that a cooking pot can hold."""
    )
    public static int portions = 6;

    @Override
    public String description() {
        return """
            Cooking pots allow any food item to be added. Once the combined nourishment total has reached maximum, use wooden bowls to take mixed stew from the pot.
            All negative and positive effects will be removed from the food added to the pot.""";
    }

    public static int getNumberOfPortions() {
        return Math.max(1, Math.min(64, portions));
    }
}
