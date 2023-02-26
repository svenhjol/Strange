package svenhjol.strange_archaeology.feature.stone_chests;

import svenhjol.charm.feature.variant_chests.VariantChestBlock;
import svenhjol.charm.feature.variant_chests.VariantChests;
import svenhjol.charm.feature.variant_chests.VariantTrappedChestBlock;
import svenhjol.charm_api.iface.IVariantMaterial;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange_archaeology.StrangeArchaeology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Feature(mod = StrangeArchaeology.MOD_ID)
public class StoneChests extends CharmFeature {
    public static final Map<IVariantMaterial, Supplier<VariantChestBlock>> NORMAL_CHEST_BLOCKS = new HashMap<>();
    public static final Map<IVariantMaterial, Supplier<VariantChestBlock.BlockItem>> NORMAL_CHEST_BLOCK_ITEMS = new HashMap<>();
    public static final Map<IVariantMaterial, Supplier<VariantTrappedChestBlock>> SEALED_CHEST_BLOCKS = new HashMap<>();
    public static final Map<IVariantMaterial, Supplier<VariantTrappedChestBlock.BlockItem>> SEALED_CHEST_BLOCK_ITEMS = new HashMap<>();

    @Override
    public void register() {
        for (var material : StoneChestMaterial.values()) {
            registerChest(material);
        }
    }

    public static void registerChest(IVariantMaterial material) {
        var id = material.getSerializedName() + "_chest";
        var sealedId = "sealed_" + material.getSerializedName() + "_chest";
        
        var block = StrangeArchaeology.REGISTRY.block(id, () -> new VariantChestBlock(material));
        var blockItem = StrangeArchaeology.REGISTRY.item(id, () -> new VariantChestBlock.BlockItem(block));
        var sealedBlock = StrangeArchaeology.REGISTRY.block(sealedId, () -> new VariantTrappedChestBlock(material));
        var sealedBlockItem = StrangeArchaeology.REGISTRY.item(sealedId, () -> new VariantTrappedChestBlock.BlockItem(sealedBlock));
        
        NORMAL_CHEST_BLOCKS.put(material, block);
        NORMAL_CHEST_BLOCK_ITEMS.put(material, blockItem);
        
        SEALED_CHEST_BLOCKS.put(material, sealedBlock);
        SEALED_CHEST_BLOCK_ITEMS.put(material, sealedBlockItem);
        
        StrangeArchaeology.REGISTRY.blockEntityBlocks(VariantChests.NORMAL_BLOCK_ENTITY, List.of(block));
        StrangeArchaeology.REGISTRY.blockEntityBlocks(VariantChests.TRAPPED_BLOCK_ENTITY, List.of(sealedBlock));
    }
}
