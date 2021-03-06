package svenhjol.strange.module.rubble;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LootHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeLoot;

@CommonModule(mod = Strange.MOD_ID, description = "Rubble contains a valuable item.  Use a shovel as if you were drawing a bow to start extracting it.")
public class Rubble extends CharmModule {
    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "rubble");
    public static final ResourceLocation TRIGGER_HARVESTED_RUBBLE = new ResourceLocation(Strange.MOD_ID, "harvested_rubble");
    public static RubbleBlock RUBBLE;
    public static BlockEntityType<RubbleBlockEntity> BLOCK_ENTITY;

    public RubbleClient client;

    @Override
    public void register() {
        RUBBLE = new RubbleBlock(this);
        BLOCK_ENTITY = RegistryHelper.blockEntity(BLOCK_ID, RubbleBlockEntity::new, RUBBLE);
    }

    @Override
    public void runWhenEnabled() {
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUBBLE);
    }

    public static void triggerHarvestedRubble(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_HARVESTED_RUBBLE);
    }
}
