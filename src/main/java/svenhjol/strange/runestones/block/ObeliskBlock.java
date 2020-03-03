package svenhjol.strange.runestones.block;

import net.minecraft.block.Blocks;
import svenhjol.meson.MesonModule;

public class ObeliskBlock extends BaseRunestoneBlock
{
    public ObeliskBlock(MesonModule module, int runeValue)
    {
        super(module, "obelisk", runeValue, Properties.from(Blocks.STONE));
    }
}
