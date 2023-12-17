package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public abstract class BaseScreen extends Screen {
    static final ResourceLocation BACKGROUND = new ResourceLocation(Strange.ID, "textures/gui/travel_journal.png");

    protected int midX;
    protected int midY;
    protected int backgroundWidth;
    protected int backgroundHeight;

    protected BaseScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null) {
            return;
        }

        midX = width / 2;
        midY = height / 2;

        backgroundWidth = 256;
        backgroundHeight = 208;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        guiGraphics.blit(getBackgroundTexture(), x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    protected ResourceLocation getBackgroundTexture() {
        return BACKGROUND;
    }
}
