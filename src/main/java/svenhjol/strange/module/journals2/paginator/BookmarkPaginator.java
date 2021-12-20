package svenhjol.strange.module.journals2.paginator;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals2.screen.bookmark.JournalBookmarkScreen;

import java.util.List;
import java.util.function.Consumer;

public class BookmarkPaginator extends Paginator<Bookmark> {
    public BookmarkPaginator(List<Bookmark> items) {
        super(items);
    }

    @Override
    protected Component getItemName(Bookmark item) {
        return new TextComponent(item.getName());
    }

    @Override
    protected Consumer<Bookmark> getItemClickAction(Bookmark item) {
        return i -> ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalBookmarkScreen(i)));
    }

    @Nullable
    @Override
    protected ItemStack getItemIcon(Bookmark item) {
        return new ItemStack(Registry.ITEM.get(item.getIcon()));
    }
}
