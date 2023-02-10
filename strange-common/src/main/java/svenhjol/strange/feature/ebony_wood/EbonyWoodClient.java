package svenhjol.strange.feature.ebony_wood;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm_core.annotation.ClientFeature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeClient;

import java.util.List;
import java.util.function.BooleanSupplier;

@ClientFeature
public class EbonyWoodClient extends CharmFeature {
    @Override
    public List<BooleanSupplier> checks() {
        return List.of(() -> Strange.LOADER.isEnabled(EbonyWood.class));
    }

    @Override
    public void register() {
        // Cut out transparent areas of blocks.
        StrangeClient.REGISTRY.blockRenderType(EbonyWood.DOOR_BLOCK, RenderType::cutout);
        StrangeClient.REGISTRY.blockRenderType(EbonyWood.TRAPDOOR_BLOCK, RenderType::cutout);
        StrangeClient.REGISTRY.blockRenderType(EbonyWood.SAPLING_BLOCK, RenderType::cutout);

        // Register boat models.
        StrangeClient.REGISTRY.modelLayer(
            () -> new ModelLayerLocation(Strange.makeId("boat/ebony"), "main"),
            BoatModel::createBodyModel);

        StrangeClient.REGISTRY.modelLayer(
            () -> new ModelLayerLocation(Strange.makeId("chest_boat/ebony"), "main"),
            ChestBoatModel::createBodyModel);

        // Register sign material.
        StrangeClient.REGISTRY.signMaterial(EbonyWood.WOOD_TYPE);

        // Register foliage colors.
        StrangeClient.REGISTRY.itemColor(this::handleItemColor, List.of(EbonyWood.LEAVES_BLOCK));
        StrangeClient.REGISTRY.blockColor(this::handleBlockColor, List.of(EbonyWood.LEAVES_BLOCK));

        if (isEnabled()) {
            // Add the ebony sapling to the natural blocks tab.
            StrangeClient.REGISTRY.itemTab(
                EbonyWood.SAPLING_BLOCK,
                CreativeModeTabs.NATURAL_BLOCKS,
                Items.SPRUCE_SAPLING
            );
        }
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
