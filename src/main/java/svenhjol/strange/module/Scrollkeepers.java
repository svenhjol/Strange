package svenhjol.strange.module;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.VillagerHelper;
import svenhjol.meson.iface.Module;
import svenhjol.meson.mixin.accessor.RenderLayersAccessor;
import svenhjol.strange.Strange;
import svenhjol.strange.block.WritingDeskBlock;

@Module(description = "Scrollkeepers are villagers that sell scrolls and accept completed quests.")
public class Scrollkeepers extends MesonModule {
    public static Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "writing_desk");
    public static Identifier VILLAGER_ID = new Identifier(Strange.MOD_ID, "scrollkeeper");

    public static WritingDeskBlock WRITING_DESK;
    public static VillagerProfession SCROLLKEEPER;
    public static PointOfInterestType POIT;

    @Override
    public void register() {
        // TODO: dedicated sounds for scrollkeeper jobsite
        WRITING_DESK = new WritingDeskBlock(this);
        POIT = VillagerHelper.addPointOfInterestType(BLOCK_ID, WRITING_DESK, 1);
        SCROLLKEEPER = VillagerHelper.addProfession(VILLAGER_ID, POIT, SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);

        // TODO: village builds for scrollkeepers
    }

    @Override
    public void clientRegister() {
        RenderLayersAccessor.getBlocks().put(WRITING_DESK, RenderLayer.getCutout());
    }
}
