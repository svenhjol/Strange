package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import svenhjol.charm.base.CharmModule;

public class WolfBlock extends BaseFrameBlock {
    public WolfBlock(CharmModule module) {
        super(module, "wolf", FabricBlockSettings.copy(Blocks.STONE));
    }


}
