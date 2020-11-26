package svenhjol.strange.scrolls.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import svenhjol.strange.base.helper.GuiHelper;
import svenhjol.strange.scrolls.tag.Quest;

public abstract class Panel extends GuiHelper {
    protected int textColor = 0xFFFFFF;
    protected int titleColor = 0xFFFF44;
    protected int rowHeight = 16;

    public abstract void render(Screen screen, MatrixStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY);
}
