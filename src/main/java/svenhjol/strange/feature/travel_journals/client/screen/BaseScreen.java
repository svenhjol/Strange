package svenhjol.strange.feature.travel_journals.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.charmony.Log;
import svenhjol.charm.charmony.Resolve;
import svenhjol.charm.charmony.feature.FeatureResolver;
import svenhjol.strange.feature.travel_journals.TravelJournals;
import svenhjol.strange.feature.travel_journals.TravelJournalsClient;
import svenhjol.strange.feature.travel_journals.client.ClientHelpers;
import svenhjol.strange.feature.travel_journals.client.Resources;
import svenhjol.strange.feature.travel_journals.common.BookmarkData;
import svenhjol.strange.feature.travel_journals.common.JournalData;

public abstract class BaseScreen extends Screen implements FeatureResolver<TravelJournalsClient> {    
    protected int midX;
    protected int backgroundWidth;
    protected int backgroundHeight;
    
    protected static final DataComponentType<JournalData> JOURNAL_DATA = 
        Resolve.feature(TravelJournals.class).registers.journalData.get();

    protected static final DataComponentType<BookmarkData> BOOKMARK_DATA =
        Resolve.feature(TravelJournals.class).registers.bookmarkData.get();
    
    public BaseScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();
        
        if (minecraft == null) return;

        midX = width / 2;
        backgroundWidth = Resources.BACKGROUND_DIMENSIONS.getFirst();
        backgroundHeight = Resources.BACKGROUND_DIMENSIONS.getSecond();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTitle(guiGraphics, midX, 24);
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);

        int x = (width - backgroundWidth) / 2;
        int y = 5;
        guiGraphics.blit(getBackgroundTexture(), x, y, 0, 0, backgroundWidth, backgroundHeight);
    }
    
    @Override
    public void onClose() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(feature().linked().registers.interactSound.get(), 0.5f, 1.0f);
        }
        super.onClose();
    }

    @Override
    public Class<TravelJournalsClient> typeForFeature() {
        return TravelJournalsClient.class;
    }
    
    protected Log log() {
        return feature().log();
    }

    protected void renderTitle(GuiGraphics guiGraphics, int x, int y) {
        ClientHelpers.drawCenteredString(guiGraphics, font, getTitle(), x, y, 0x702f20, false);
    }
    
    protected ResourceLocation getBackgroundTexture() {
        return Resources.BACKGROUND;
    }
    
    protected Minecraft minecraft() {
        return Minecraft.getInstance();
    }
}
