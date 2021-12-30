package svenhjol.strange.module.journals.screen.mini;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.journals.paginator.BookmarkPaginator;
import svenhjol.strange.module.journals.screen.JournalResources;
import svenhjol.strange.module.journals.screen.MiniJournal;
import svenhjol.strange.module.runes.client.RuneStringRenderer;

public class MiniBookmarksScreen extends BaseMiniScreen {
    public static final Component INCORRECT_DIMENSION;

    private BookmarkPaginator paginator;
    private RuneStringRenderer runeStringRenderer;

    public MiniBookmarksScreen(MiniJournal mini) {
        super(mini);
    }

    @Override
    public void init() {
        super.init();

        if (mini.selectedBookmark != null) {
            // If the selected bookmark is not in the player's dimension then return early.
            if (!validDimension(mini.selectedBookmark)) {
                mini.selectedBookmark = null;
                return;
            }

            runeStringRenderer = new RuneStringRenderer(journalMidX - 46, midY - 8, 9, 14, 10, 4);

            mini.addBackButton(b -> {
                mini.selectedBookmark = null;
                mini.changeSection(MiniJournal.Section.BOOKMARKS);
            });

        } else {

            var branch = BookmarksClient.getBranch();
            if (branch.isEmpty()) return;

            paginator = new BookmarkPaginator(branch.get().values(minecraft.player.getUUID()));
            setPaginatorDefaults(paginator);

            // If the listed bookmark is not in the player's dimension then disable it and show a tooltip to this effect.
            paginator.setOnItemHovered(bookmark -> validDimension(bookmark) ? new TextComponent(bookmark.getName()) : INCORRECT_DIMENSION);
            paginator.setOnItemButtonRendered((bookmark, button) -> button.active = validDimension(bookmark));

            paginator.init(screen, mini.offset, midX - 87, midY - 78, bookmark -> {
                mini.selectedBookmark = bookmark;
                mini.redraw();
            }, newOffset -> {
                mini.offset = newOffset;
                mini.redraw();
            });

            mini.addBackButton(b -> mini.changeSection(MiniJournal.Section.HOME));

        }
    }

    @Override
    public void render(PoseStack poseStack, ItemRenderer itemRenderer, Font font) {
        mini.renderTitle(poseStack, JournalResources.BOOKMARKS, midY - 94);

        if (mini.selectedBookmark != null) {
            runeStringRenderer.render(poseStack, font, mini.selectedBookmark.getRunes());
        } else {
            paginator.render(poseStack, itemRenderer, font);
        }
    }

    private boolean validDimension(Bookmark bookmark) {
        return DimensionHelper.isDimension(minecraft.level, bookmark.getDimension());
    }

    static {
        INCORRECT_DIMENSION = new TranslatableComponent("gui.strange.journal.incorrect_dimension");
    }
}
