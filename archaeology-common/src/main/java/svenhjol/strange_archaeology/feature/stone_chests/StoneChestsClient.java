package svenhjol.strange_archaeology.feature.stone_chests;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import svenhjol.charm.feature.variant_chests.*;
import svenhjol.charm_api.event.BlockItemRenderEvent;
import svenhjol.charm_api.iface.IVariantMaterial;
import svenhjol.charm_core.annotation.ClientFeature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange_archaeology.StrangeArchaeology;
import svenhjol.strange_archaeology.StrangeArchaeologyClient;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@ClientFeature
public class StoneChestsClient extends CharmFeature {
    private VariantChestBlockEntity CACHED_NORMAL_CHEST;
    private VariantChestBlockEntity CACHED_SEALED_CHEST;
    
    @Override
    public List<BooleanSupplier> checks() {
        return List.of(() -> StrangeArchaeology.LOADER.isEnabled(StoneChests.class));
    }

    @Override
    public void register() {
        StrangeArchaeologyClient.REGISTRY.blockEntityRenderer(VariantChests.NORMAL_BLOCK_ENTITY,
            () -> StoneChestBlockEntityRenderer::new);
        StrangeArchaeologyClient.REGISTRY.blockEntityRenderer(VariantChests.TRAPPED_BLOCK_ENTITY,
            () -> StoneChestBlockEntityRenderer::new);
        
        if (isEnabled()) {
            for (var item : StoneChests.NORMAL_CHEST_BLOCK_ITEMS.values()) {
                StrangeArchaeologyClient.REGISTRY.itemTab(
                    item,
                    CreativeModeTabs.FUNCTIONAL_BLOCKS,
                    Items.CHEST
                );
            }
        }
    }

    @Override
    public void runWhenEnabled() {
        CACHED_NORMAL_CHEST = new VariantChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
        CACHED_SEALED_CHEST = new VariantTrappedChestBlockEntity(BlockPos.ZERO, Blocks.TRAPPED_CHEST.defaultBlockState());

        BlockItemRenderEvent.INSTANCE.handle(this::handleRenderBlockItem);

        // Add all custom textures to a reference map.
        for (IVariantMaterial material : StoneChests.NORMAL_CHEST_BLOCKS.keySet()) {
            String[] bases = {"sealed", "normal"};
            ChestType[] chestTypes = {ChestType.SINGLE, ChestType.LEFT, ChestType.RIGHT};

            for (String base : bases) {
                for (ChestType chestType : chestTypes) {
                    var chestTypeName = chestType == ChestType.SINGLE ? "" : "_" + chestType.getSerializedName().toLowerCase();
                    var textureId = StrangeArchaeology.makeId("entity/chest/" + material.getSerializedName() + "_" + base + chestTypeName);

                    // Store the texture reference in the chest renderer.
                    StoneChestBlockEntityRenderer.addTexture(material, chestType, textureId, base.equals("sealed"));
                }
            }
        }
    }

    private Optional<BlockEntity> handleRenderBlockItem(ItemStack itemStack, Block block) {
        if (block instanceof VariantChestBlock chest) {
            CACHED_NORMAL_CHEST.setMaterial(chest.getMaterial());
            return Optional.of(CACHED_NORMAL_CHEST);
        } else if (block instanceof VariantTrappedChestBlock chest) {
            CACHED_SEALED_CHEST.setMaterial(chest.getMaterial());
            return Optional.of(CACHED_SEALED_CHEST);
        }

        return Optional.empty();
    }
}
