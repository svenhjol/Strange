package svenhjol.strange.feature.travel_journal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

public class TravelJournalResources {
    public static final ResourceLocation JOURNAL_BACKGROUND = new ResourceLocation(Strange.ID, "textures/gui/travel_journal.png");
    public static final Pair<Integer, Integer> JOURNAL_BACKGROUND_DIM = Pair.of(256, 208);
    public static final Component HOME_TITLE = TextHelper.translatable("gui.strange.travel_journal.travel_journal");
    public static final Component CLOSE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.close");
    public static final Component BACK_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.back");
    public static final Component HOME_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.home");
    public static final Component SAVE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.save");
    public static final Component CANCEL_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.cancel");
    public static final Component DELETE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.delete");
    public static final Component NEXT_PAGE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.next_page");
    public static final Component PREVIOUS_PAGE_BUTTON_TEXT = TextHelper.translatable("gui.strange.travel_journal.previous_page");
}
