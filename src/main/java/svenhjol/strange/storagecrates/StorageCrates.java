package svenhjol.strange.storagecrates;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

@Module(mod = Strange.MOD_ID, client = StorageCratesClient.class)
public class StorageCrates extends CharmModule {
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "storage_crate");
    public static StorageCrateBlock STORAGE_CRATE;
    public static BlockEntityType<StorageCrateBlockEntity> BLOCK_ENTITY;

    @Override
    public void register() {
        STORAGE_CRATE = new StorageCrateBlock(this);
        BLOCK_ENTITY = RegistryHandler.blockEntity(ID, StorageCrateBlockEntity::new, STORAGE_CRATE);
    }
}
