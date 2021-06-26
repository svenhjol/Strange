package svenhjol.strange.module.scrolls.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.scrolls.nbt.Quest;

public abstract class BasePanel extends GuiHelper {
    protected int textColor = 0xFFFFFF;
    protected int titleColor = 0xFFFF44;
    protected int rowHeight = 16;

    public abstract void render(Screen screen, PoseStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY);
}
