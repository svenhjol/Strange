package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.network.chat.Component;
import svenhjol.charmony.helper.TextHelper;

public class HomeScreen extends BaseScreen {
    static final Component TITLE = TextHelper.translatable("gui.strange.travel_journal.home");

    public HomeScreen() {
        super(TITLE);
    }
}
