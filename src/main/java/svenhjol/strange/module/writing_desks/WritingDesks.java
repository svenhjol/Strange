package svenhjol.strange.module.writing_desks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@CommonModule(mod = Strange.MOD_ID, priority = 10)
public class WritingDesks extends CharmModule {
    public static final ResourceLocation WRITING_DESK_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "writing_desk");

    public static Map<String, ResourceLocation> TRIGGERS = new HashMap<>();

    public static WritingDeskBlock WRITING_DESK;
    public static MenuType<WritingDeskMenu> WRITING_DESK_MENU;

    public static Map<UUID, String> writtenRunes = new WeakHashMap<>();

    public static void triggerWriteTome(ServerPlayer player, String branch) {
        if (TRIGGERS.containsKey(branch)) {
            CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGERS.get(branch));
        }
    }

    @Override
    public void register() {
        WRITING_DESK = new WritingDeskBlock(this);
        WRITING_DESK_MENU = CommonRegistry.menu(WRITING_DESK_BLOCK_ID, WritingDeskMenu::new);

        TRIGGERS.put(BiomeBranch.NAME, new ResourceLocation(Strange.MOD_ID, "write_biome_tome"));
        TRIGGERS.put(BookmarkBranch.NAME, new ResourceLocation(Strange.MOD_ID, "write_bookmark_tome"));
        TRIGGERS.put(DimensionBranch.NAME, new ResourceLocation(Strange.MOD_ID, "write_dimension_tome"));
        TRIGGERS.put(StructureBranch.NAME, new ResourceLocation(Strange.MOD_ID, "write_structure_tome"));
    }
}
