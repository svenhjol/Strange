package svenhjol.strange.scroll;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.scroll.tag.QuestTag;

public abstract class BasePanel extends GuiHelper {
    protected int textColor = 0xFFFFFF;
    protected int titleColor = 0xFFFF00;
    protected int rowHeight = 16;

    public abstract void render(Screen screen, MatrixStack matrices, QuestTag quest, int mid, int width, int top, int mouseX, int mouseY);
}
