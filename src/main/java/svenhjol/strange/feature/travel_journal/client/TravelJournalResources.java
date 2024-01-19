package svenhjol.strange.feature.travel_journal.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

public class TravelJournalResources {
    public static final ResourceLocation JOURNAL_BACKGROUND = new ResourceLocation(Strange.ID, "textures/gui/travel_journal.png");
    public static final Pair<Integer, Integer> JOURNAL_BACKGROUND_DIM = Pair.of(256, 208);
    public static final WidgetSprites BOOKMARKS_BUTTON = makeButton("bookmarks");
    public static final WidgetSprites HOME_BUTTON = makeButton("home");
    public static final WidgetSprites LEARNED_BUTTON = makeButton("learned");
    public static final WidgetSprites QUESTS_BUTTON = makeButton("quests");
    public static final WidgetSprites NOVICE_SCROLL_BUTTON = makeButton("novice_scroll");
    public static final WidgetSprites APPRENTICE_SCROLL_BUTTON = makeButton("apprentice_scroll");
    public static final WidgetSprites JOURNEYMAN_SCROLL_BUTTON = makeButton("journeyman_scroll");
    public static final WidgetSprites EXPERT_SCROLL_BUTTON = makeButton("expert_scroll");
    public static final WidgetSprites MASTER_SCROLL_BUTTON = makeButton("master_scroll");
    public static final WidgetSprites SAVE_TO_BOOKMARK_BUTTON = makeButton("save_to_bookmark");
    public static final WidgetSprites SAVE_TO_MAP_BUTTON = makeButton("save_to_map");
    public static final WidgetSprites PHOTO_BUTTON = makeButton("photo");
    public static final WidgetSprites TRASH_BUTTON = makeButton("trash");
    public static final WidgetSprites NEXT_PAGE_BUTTON = makeButton("next_page");
    public static final WidgetSprites PREVIOUS_PAGE_BUTTON = makeButton("previous_page");
    public static final WidgetSprites EDIT_BUTTON = makeButton("edit");
    public static final Component HOME_TITLE = TextHelper.translatable("gui.strange.travel_journal.travel_journal");
    public static final Component LEARNED_TITLE = TextHelper.translatable("gui.strange.travel_journal.learned");
    public static final Component BOOKMARKS_TITLE = TextHelper.translatable("gui.strange.travel_journal.bookmarks");
    public static final Component QUESTS_TITLE = TextHelper.translatable("gui.strange.travel_journal.quests");
    public static final Component CHANGE_ICON_TITLE = TextHelper.translatable("gui.strange.travel_journal.change_icon");
    public static final Component CHANGE_NAME_TITLE = TextHelper.translatable("gui.strange.travel_journal.change_name");
    public static final Component BOOKMARKS_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.bookmarks");
    public static final Component QUESTS_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.quests");
    public static final Component NEW_BOOKMARK_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.new_bookmark");
    public static final Component CLOSE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.close");
    public static final Component BACK_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.back");
    public static final Component HOME_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.home");
    public static final Component SAVE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.save");
    public static final Component CANCEL_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.cancel");
    public static final Component TAKE_PHOTO_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.take_photo");
    public static final Component TAKE_NEW_PHOTO_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.take_new_photo");
    public static final Component CHANGE_NAME_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.change_name");
    public static final Component CHANGE_ICON_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.change_icon");
    public static final Component DELETE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.delete");
    public static final Component LEARNED_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.learned");
    public static final Component NEXT_PAGE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.next_page");
    public static final Component PREVIOUS_PAGE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.previous_page");
    public static final Component SAVE_TO_MAP_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.save_to_map");
    public static final Component SAVE_TO_BOOKMARK_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.save_to_bookmark");
    public static final Component NO_LEARNED_LOCATIONS_HEADING_TEXT = TextHelper.translatable("gui.strange.travel_journal.no_learned_locations.heading");
    public static final Component NO_LEARNED_LOCATIONS_BODY_TEXT = TextHelper.translatable("gui.strange.travel_journal.no_learned_locations.body");
    public static final Component NO_QUESTS_TEXT = TextHelper.translatable("gui.strange.travel_journal.no_quests");

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/travel_journal/" + name + "_button"),
            instance.id("widget/travel_journal/" + name + "_button_highlighted"));
    }

}
