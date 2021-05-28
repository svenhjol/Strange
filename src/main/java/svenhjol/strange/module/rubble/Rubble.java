package svenhjol.strange.module.rubble;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.helper.LootHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.module.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeLoot;

@Module(mod = Strange.MOD_ID, client = RubbleClient.class, description = "Rubble contains a valuable item. Hold down a shovel to start extracting it.")
public class Rubble extends CharmModule {
    public static final Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "rubble");
    public static final Identifier TRIGGER_HARVESTED_RUBBLE = new Identifier(Strange.MOD_ID, "harvested_rubble");
    public static RubbleBlock RUBBLE;
    public static BlockEntityType<RubbleBlockEntity> BLOCK_ENTITY;

    public RubbleClient client;

    @Override
    public void register() {
        RUBBLE = new RubbleBlock(this);
        BLOCK_ENTITY = RegistryHelper.blockEntity(BLOCK_ID, RubbleBlockEntity::new, RUBBLE);
    }

    @Override
    public void init() {
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUBBLE);
    }

    public static void triggerHarvestedRubble(ServerPlayerEntity player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_HARVESTED_RUBBLE);
    }
}
