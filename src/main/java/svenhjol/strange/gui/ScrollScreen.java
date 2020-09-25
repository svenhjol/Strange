package svenhjol.strange.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.scroll.ScrollQuest;

public class ScrollScreen extends Screen {
    protected ScrollScreen(Text title) {
        super(title);
    }

    public ScrollScreen(ScrollQuest quest) {
        this(new TranslatableText(quest.getTitle()));
    }
}
