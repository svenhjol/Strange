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
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "ancient_rubble");
    public static AncientRubbleBlock ANCIENT_RUBBLE;
    public static BlockEntityType<AncientRubbleBlockEntity> BLOCK_ENTITY;

    public ExcavationClient client;

    @Override
    public void register() {
        ANCIENT_RUBBLE = new AncientRubbleBlock(this);
        BLOCK_ENTITY = RegistryHandler.blockEntity(ID, AncientRubbleBlockEntity::new, ANCIENT_RUBBLE);
    }

    @Override
    public void init() {
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.ANCIENT_RUBBLE);
    }
}
