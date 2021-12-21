package svenhjol.strange.module.journals.paginator;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.bookmarks.Bookmark;

import java.util.List;

public class BookmarkPaginator extends BasePaginator<Bookmark> {
    public BookmarkPaginator(List<Bookmark> items) {
        super(items);
    }

    @Override
    protected Component getItemName(Bookmark item) {
        return new TextComponent(item.getName());
    }

    @Nullable
    @Override
    protected ItemStack getItemIcon(Bookmark item) {
        return new ItemStack(Registry.ITEM.get(item.getIcon()));
    }
}
