package svenhjol.strange.module.journals.paginator;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.discoveries.DiscoveryHelper;

import java.util.List;

public class DiscoveryPaginator extends BasePaginator<Discovery> {
    public DiscoveryPaginator(List<Discovery> items) {
        super(items);
    }

    @Override
    protected Component getItemName(Discovery item) {
        var name = DiscoveryHelper.getDiscoveryName(item);
        Component component;

        if (item.getTime() != 0L) {
            long time = item.getTime() / 24000L;
            component = new TextComponent(I18n.get("gui.strange.journal.discovery_with_days", name, time));
        } else {
            component = new TextComponent(name);
        }

        return component;
    }

    @Nullable
    @Override
    protected ItemStack getItemIcon(Discovery item) {
        return null;
    }
}
