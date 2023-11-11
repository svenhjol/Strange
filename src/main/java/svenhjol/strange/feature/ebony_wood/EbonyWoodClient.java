package svenhjol.strange.feature.ebony_wood;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.feature.custom_wood.CustomWood;

import java.util.List;

public class EbonyWoodClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return EbonyWood.class;
    }

    @Override
    public void register() {
        var registry = mod().registry();
        var holder = CustomWood.getHolder(EbonyMaterial.EBONY);

        var door = holder.getDoor().orElseThrow();
        var leaves = holder.getLeaves().orElseThrow();
        var sapling = holder.getSapling().orElseThrow();
        var trapdoor = holder.getTrapdoor().orElseThrow();

        // Cut out transparent areas.
        registry.blockRenderType(door.block, RenderType::cutout);
        registry.blockRenderType(sapling.block, RenderType::cutout);
        registry.blockRenderType(trapdoor.block, RenderType::cutout);

        // Foliage colors.
        registry.itemColor(this::handleItemColor, List.of(leaves.block));
        registry.blockColor(this::handleBlockColor, List.of(leaves.block));
    }

    private int handleItemColor(ItemStack stack, int tintIndex) {
        var state = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
        var blockColors = Minecraft.getInstance().getBlockColors();
        return blockColors.getColor(state, null, null, tintIndex);
    }

    private int handleBlockColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        return level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor();
    }
}
