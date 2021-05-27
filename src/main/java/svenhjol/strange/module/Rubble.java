package svenhjol.strange.module;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeLoot;
import svenhjol.strange.block.RubbleBlock;
import svenhjol.strange.block.entity.RubbleBlockEntity;
import svenhjol.strange.client.module.RubbleClient;

@Module(mod = Strange.MOD_ID, client = RubbleClient.class)
public class Rubble extends CharmModule {
    public static final Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "rubble");
    public static RubbleBlock RUBBLE;
    public static BlockEntityType<RubbleBlockEntity> BLOCK_ENTITY;

    public RubbleClient client;

    @Override
    public void register() {
        RUBBLE = new RubbleBlock(this);
        BLOCK_ENTITY = RegistryHandler.blockEntity(BLOCK_ID, RubbleBlockEntity::new, RUBBLE);
    }

    @Override
    public void init() {
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUBBLE);
    }
}
