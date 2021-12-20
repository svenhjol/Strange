package svenhjol.strange.module.journals2.screen.mini;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import svenhjol.strange.module.journals2.screen.MiniJournal;

public abstract class BaseMiniScreen {
    protected MiniJournal mini;
    protected Screen screen;
    protected Minecraft minecraft;

    protected int midX;
    protected int midY;
    protected int journalMidX;
    protected int buttonWidth;
    protected int buttonHeight;

    /**
     * Constructor always needs a reference back to the existing mini journal container class.
     * The mini journal has a reference to its associated screen so we make that available here.
     */
    public BaseMiniScreen(MiniJournal mini) {
        this.mini = mini;
        this.screen = mini.getScreen();
    }

    /**
     * At this point the minecraft client has been referenced and width and height are established.
     */
    public void init() {
        this.minecraft = mini.getMinecraft();
        this.midX = mini.midX;
        this.midY = mini.midY;
        this.journalMidX = mini.journalMidX;
        this.buttonWidth = mini.buttonWidth;
        this.buttonHeight = mini.buttonHeight;
    }

    /**
     * This gets rendered only in the first rendering pass.
     */
    public void firstRender(PoseStack poseStack, Font font, ItemRenderer itemRenderer) {
        // no op
    }

    /**
     * This gets rendererd every pass.
     */
    public abstract void render(PoseStack poseStack, ItemRenderer itemRenderer, Font font);
}
