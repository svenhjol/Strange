package svenhjol.strange.bronze;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ShieldItem;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.ICharmItem;

public class ReinforcedShieldItem extends ShieldItem implements ICharmItem {
    private final CharmModule module;

    public ReinforcedShieldItem(CharmModule module) {
        super((new Settings()).maxDamage(990).group(ItemGroup.COMBAT));
        this.register(module, "reinforced_shield");
        this.module = module;
    }

    @Override
    public boolean enabled() {
        return module.enabled;
    }
}
