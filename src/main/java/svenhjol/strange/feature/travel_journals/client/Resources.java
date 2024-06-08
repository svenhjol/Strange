package svenhjol.strange.feature.travel_journals.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public final class Resources {
    public static final Pair<Integer, Integer> BACKGROUND_DIMENSIONS = Pair.of(256, 208);
    public static final ResourceLocation BACKGROUND = Strange.id("textures/gui/travel_journal.png");
    public static final Component BOOKMARKS = Component.translatable("gui.strange.travel_journals.bookmarks");
    public static final Component BOOKMARKS_TITLE = Component.translatable("gui.strange.travel_journals.bookmarks");
    public static final Component CHANGE_COLOR_BUTTON_TEXT = Component.translatable("gui.strange.travel_journals.change_color");
    public static final Component CHANGE_COLOR_TITLE = Component.translatable("gui.strange.travel_journals.change_color");
    public static final Component CHANGE_NAME_TITLE = Component.translatable("gui.strange.travel_journals.change_name");
    public static final Component CREATED_BY_TEXT = Component.translatable("gui.strange.travel_journals.created_by");
    public static final Component DELETE_BOOKMARK_BUTTON_TEXT = Component.translatable("gui.strange.travel_journals.delete_bookmark");
    public static final Component DETAILS = Component.translatable("gui.strange.travel_journals.details");
    public static final Component DESCRIPTION = Component.translatable("gui.strange.travel_journals.description");
    public static final Component NAME_TEXT = Component.translatable("gui.strange.travel_journals.name");
    public static final Component EDIT_NAME = Component.translatable("gui.strange.travel_journals.edit_name");
    public static final Component EDIT_DESCRIPTION = Component.translatable("gui.strange.travel_journals.edit_description");
    public static final Component TRAVEL_JOURNAL = Component.translatable("gui.strange.travel_journals.travel_journal");
    public static final Component NEW_BOOKMARK = Component.translatable("gui.strange.travel_journals.new_bookmark");
    public static final Component SAVE_TO_BOOKMARK_BUTTON_TEXT = Component.translatable("gui.strange.travel_journals.save_to_bookmark");
    public static final Component SAVE_TO_MAP_BUTTON_TEXT = Component.translatable("gui.strange.travel_journals.save_to_map");
    public static final Component TAKE_NEW_PHOTO_BUTTON_TEXT = Component.translatable("gui.strange.travel_journals.take_new_photo");
    public static final Component TAKE_PHOTO_BUTTON_TEXT = Component.translatable("gui.strange.travel_journals.take_photo");
    public static final String COORDINATES_KEY = "gui.strange.travel_journals.coordinates";
    public static final String CREATED_BY_KEY = "gui.strange.travel_journals.created_by";
}
