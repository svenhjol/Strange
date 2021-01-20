package svenhjol.strange.excavation;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;

@Module(mod = Strange.MOD_ID, client = ExcavationClient.class)
public class Excavation extends CharmModule {
    public static final Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "rubble");
    public static RubbleBlock RUBBLE;
    public static BlockEntityType<RubbleBlockEntity> BLOCK_ENTITY;

    public ExcavationClient client;

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
