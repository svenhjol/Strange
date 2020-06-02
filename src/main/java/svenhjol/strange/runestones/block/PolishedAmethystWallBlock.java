package svenhjol.strange.runestones.block;

import net.minecraft.block.WallBlock;
import net.minecraft.item.ItemGroup;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.IMesonBlock;
import svenhjol.strange.runestones.module.Amethyst;

public class PolishedAmethystWallBlock extends WallBlock implements IMesonBlock {
    public PolishedAmethystWallBlock(MesonModule module) {
        super(Properties.from(Amethyst.block));
        register(module, "polished_amethyst_wall");
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroup.BUILDING_BLOCKS;
    }

    @Override
    public boolean isEnabled() {
        return Meson.isModuleEnabled("strange:amethyst");
    }
}
