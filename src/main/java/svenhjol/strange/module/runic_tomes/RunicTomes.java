package svenhjol.strange.module.runic_tomes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID, priority = 10)
public class RunicTomes extends CharmModule {
    public static final ResourceLocation WRITING_DESK_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "writing_desk");
    public static RunicTomeItem RUNIC_TOME;
    public static WritingDeskBlock WRITING_DESK;
    public static BlockEntityType<WritingDeskBlockEntity> WRITING_DESK_BLOCK_ENTITY;
    public static MenuType<WritingDeskMenu> WRITING_DESK_MENU;

    @Override
    public void register() {
        RUNIC_TOME = new RunicTomeItem(this);
        WRITING_DESK = new WritingDeskBlock(this);
        WRITING_DESK_BLOCK_ENTITY = RegistryHelper.blockEntity(WRITING_DESK_BLOCK_ID, WritingDeskBlockEntity::new);
        WRITING_DESK_MENU = RegistryHelper.screenHandler(WRITING_DESK_BLOCK_ID, WritingDeskMenu::new);
    }
}
