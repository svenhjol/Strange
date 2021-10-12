package svenhjol.strange.module.runestones.enums;

import net.minecraft.world.level.block.state.BlockBehaviour;
import svenhjol.charm.enums.ICharmEnum;

public interface IRunestoneMaterial extends ICharmEnum {
    int getId();

    BlockBehaviour.Properties getProperties();
}
