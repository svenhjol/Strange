package svenhjol.strange.feature.bookmarks.client;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

public class BookmarksResources {
    public static final Component BOOKMARKS_TITLE = TextHelper.translatable("gui.strange.bookmarks.bookmarks");
    public static final Component CHANGE_ICON_TITLE = TextHelper.translatable("gui.strange.bookmarks.change_icon");
    public static final Component CHANGE_NAME_TITLE = TextHelper.translatable("gui.strange.bookmarks.change_name");
    public static final Component NEW_BOOKMARK_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.new_bookmark");
    public static final Component BOOKMARKS_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.bookmarks");
    public static final Component TAKE_PHOTO_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.take_photo");
    public static final Component TAKE_NEW_PHOTO_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.take_new_photo");
    public static final Component CHANGE_NAME_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.change_name");
    public static final Component CHANGE_ICON_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.change_icon");
    public static final Component SAVE_TO_MAP_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.save_to_map");
    public static final Component SAVE_TO_BOOKMARK_BUTTON_TEXT = TextHelper.translatable("gui.strange.bookmarks.save_to_bookmark");
    public static final WidgetSprites BOOKMARKS_BUTTON = makeButton("bookmarks");
    public static final WidgetSprites SAVE_TO_BOOKMARK_BUTTON = makeButton("save_to_bookmark");
    public static final WidgetSprites SAVE_TO_MAP_BUTTON = makeButton("save_to_map");
    public static final WidgetSprites PHOTO_BUTTON = makeButton("photo");

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/bookmarks/" + name + "_button"),
            instance.id("widget/bookmarks/" + name + "_button_highlighted"));
    }
}
