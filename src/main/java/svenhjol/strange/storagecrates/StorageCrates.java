package svenhjol.strange.storagecrates;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.enums.IVariantMaterial;
import svenhjol.charm.base.enums.VanillaVariantMaterial;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.helper.RegistryHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID, priority = 10, client = StorageCratesClient.class)
public class StorageCrates extends CharmModule {
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "storage_crate");
    public static Map<IVariantMaterial, StorageCrateBlock> STORAGE_CRATE_BLOCKS = new HashMap<>();
    public static BlockEntityType<StorageCrateBlockEntity> BLOCK_ENTITY;

    @Config(name = "Maximum stacks", description = "Number of stacks of a single item or block that a storage crate will hold.")
    public static int maximumStacks = 54;

    @Override
    public void register() {
        BLOCK_ENTITY = RegistryHandler.blockEntity(ID, StorageCrateBlockEntity::new);

        VanillaVariantMaterial.getTypes().forEach(material -> {
            registerStorageCrate(this, material);
        });
    }

    public static StorageCrateBlock registerStorageCrate(CharmModule module, IVariantMaterial material) {
        StorageCrateBlock crate = new StorageCrateBlock(module, material);
        STORAGE_CRATE_BLOCKS.put(material, crate);
        RegistryHelper.addBlocksToBlockEntity(BLOCK_ENTITY, crate);
        return crate;
    }
}
