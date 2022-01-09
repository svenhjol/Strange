package svenhjol.strange.module.writing_desks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@CommonModule(mod = Strange.MOD_ID, priority = 10, description = "Writing Desks are the job site for scrollkeeper villagers.\n" +
    "Once you have learned enough runes, the writing desk will allow you to create a Runic Tome to a location of interest.")
public class WritingDesks extends CharmModule {
    public static final ResourceLocation WRITING_DESK_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "writing_desk");
    public static final ResourceLocation TRIGGER_WRITE_TOME = new ResourceLocation(Strange.MOD_ID, "write_tome");

    public static WritingDeskBlock WRITING_DESK;
    public static MenuType<WritingDeskMenu> WRITING_DESK_MENU;
    public static SoundEvent WRITING_DESK_SOUND;

    public static Map<UUID, String> writtenRunes = new WeakHashMap<>();

    public static void triggerWriteTome(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_WRITE_TOME);
    }

    @Override
    public void register() {
        WRITING_DESK = new WritingDeskBlock(this);
        WRITING_DESK_MENU = CommonRegistry.menu(WRITING_DESK_BLOCK_ID, WritingDeskMenu::new);
        WRITING_DESK_SOUND = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "writing_desk"));
    }
}
