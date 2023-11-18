package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;

import java.util.List;

public class CookingPotsClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return CookingPots.class;
    }

    @Override
    public void register() {
        var registry = mod().registry();

        registry.blockColor(this::handleBlockColor, List.of(CookingPots.block));
        registry.itemTab(CookingPots.block, CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.CAULDRON);
        registry.itemTab(CookingPots.mixedStewItem, CreativeModeTabs.FOOD_AND_DRINKS, Items.RABBIT_STEW);
    }

    private int handleBlockColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if (tintIndex == 0) {
            if (level != null && level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot) {
                if (pot.hasFinishedCooking()) {
                    return 0x5a2200;
                } else if (pot.hunger > 0 || pot.saturation > 0) {
                    return 0x806030;
                }
            }

            return 0x0088cc;
        }
        return -1;
    }
}
