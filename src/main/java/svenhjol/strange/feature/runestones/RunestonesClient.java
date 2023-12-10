package svenhjol.strange.feature.runestones;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charmony.client.ClientFeature;

public class RunestonesClient extends ClientFeature {
    @Override
    public void runWhenEnabled() {
        var registry = mod().registry();

        registry.itemTab(Runestones.blackstoneBlockItem, CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.LODESTONE);
        registry.itemTab(Runestones.stoneBlockItem, CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.LODESTONE);
    }
}
