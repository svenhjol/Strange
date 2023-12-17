package svenhjol.strange.feature.travel_journal;

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
    public static final WidgetSprites QUEST_BUTTON = makeButton("quest");
    public static final WidgetSprites SAVE_TO_BOOKMARK_BUTTON = makeButton("save_to_bookmark");
    public static final WidgetSprites SAVE_TO_MAP_BUTTON = makeButton("save_to_map");
    public static final WidgetSprites SCREENSHOT_BUTTON = makeButton("screenshot");
    public static final WidgetSprites TRASH_BUTTON = makeButton("trash");
    public static final Component HOME_TITLE = TextHelper.translatable("gui.strange.travel_journal.travel_journal");
    public static final Component LEARNED_TITLE = TextHelper.translatable("gui.strange.travel_journal.learned");
    public static final Component BOOKMARKS_TITLE = TextHelper.translatable("gui.strange.travel_journal.bookmarks");
    public static final Component BOOKMARKS_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.bookmarks");
    public static final Component CLOSE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.close");
    public static final Component HOME_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.home");
    public static final Component LEARNED_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.learned");

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/travel_journal/" + name + "_button"),
            instance.id("widget/travel_journal/" + name + "_button_highlighted"));
    }
}
