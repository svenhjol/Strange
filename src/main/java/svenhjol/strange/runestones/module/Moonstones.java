package svenhjol.strange.runestones.module;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.DyeColor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.charm.base.CharmCategories;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.block.MoonstoneBlock;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = CharmCategories.DECORATION)
public class Moonstones extends MesonModule {
    public static List<MoonstoneBlock> moonstones = new ArrayList<>();

    @Override
    public void init() {
        DyeColor[] colors = DyeColor.values();

        for (DyeColor color : colors) {
            moonstones.add(new MoonstoneBlock(this, color));
        }
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        moonstones.forEach(block -> RenderTypeLookup.setRenderLayer(block, RenderType.getTranslucent()));
    }
}
