package svenhjol.strange.feature.learned_runes.client;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

public class LearnedRunesResources {
    public static final Component LEARNED_BUTTON_TEXT = TextHelper.translatable("gui.strange.learned_runes.learned");
    public static final Component LEARNED_TITLE = TextHelper.translatable("gui.strange.learned_runes.learned");
    public static final Component NO_LEARNED_LOCATIONS_HEADING_TEXT = TextHelper.translatable("gui.strange.learned_runes.no_learned_locations.heading");
    public static final Component NO_LEARNED_LOCATIONS_BODY_TEXT = TextHelper.translatable("gui.strange.learned_runes.no_learned_locations.body");

    public static final WidgetSprites LEARNED_BUTTON = makeButton("runes");

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/learned_runes/" + name + "_button"),
            instance.id("widget/learned_runes/" + name + "_button_highlighted"));
    }
}
