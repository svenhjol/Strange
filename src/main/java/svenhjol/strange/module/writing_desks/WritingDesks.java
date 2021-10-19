package svenhjol.strange.module.writing_desks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@CommonModule(mod = Strange.MOD_ID)
public class WritingDesks extends CharmModule {
    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "writing_desk");
    public static WritingDeskBlock WRITING_DESK;
    public static MenuType<WritingDeskMenu> MENU;

    public static final ResourceLocation MSG_CLIENT_VALID_RUNES = new ResourceLocation(Strange.MOD_ID, "client_valid_runes");

    public static Map<UUID, String> writtenRunes = new WeakHashMap<>();

    @Override
    public void register() {
        WRITING_DESK = new WritingDeskBlock(this);
        MENU = RegistryHelper.screenHandler(BLOCK_ID, WritingDeskMenu::new);
    }
}
