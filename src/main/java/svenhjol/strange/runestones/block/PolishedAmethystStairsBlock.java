package svenhjol.strange.runestones.block;

import net.minecraft.block.StairsBlock;
import net.minecraft.item.ItemGroup;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.IMesonBlock;
import svenhjol.strange.runestones.module.Amethyst;

public class PolishedAmethystStairsBlock extends StairsBlock implements IMesonBlock {
    public PolishedAmethystStairsBlock(MesonModule module) {
        super(Amethyst.block.getDefaultState(), Properties.from(Amethyst.block));
        register(module, "polished_amethyst_stairs");
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
