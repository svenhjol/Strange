package svenhjol.strange.bronze;

import net.minecraft.item.ShieldItem;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

@Module(mod = Strange.MOD_ID, client = BronzeClient.class)
public class Bronze extends CharmModule {
    public static ShieldItem REINFORCED_SHIELD;

    @Override
    public void register() {
        REINFORCED_SHIELD = new ReinforcedShieldItem(this);
    }

    @Override
    public boolean depends() {
        return ModuleHandler.enabled("charm:bronze");
    }

    @Override
    public List<Identifier> getRecipesToRemove() {
        List<Identifier> remove = new ArrayList<>();

        if (!ModuleHandler.enabled("charm:bronze")) {
            remove.add(new Identifier(Strange.MOD_ID, "bronze/reinforced_shield"));
        }

        return remove;
    }
}
