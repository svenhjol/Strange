package svenhjol.strange.module.journals2.paginator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.StringHelper;

import java.util.List;

public abstract class ResourcePaginator extends BasePaginator<ResourceLocation> {
    public ResourcePaginator(List<ResourceLocation> items) {
        super(items);
    }

    @Override
    protected Component getItemName(ResourceLocation item) {
        return new TextComponent(StringHelper.snakeToPretty(item.getPath(), true));
    }

    @Nullable
    @Override
    protected ItemStack getItemIcon(ResourceLocation item) {
        return null;
    }
}
