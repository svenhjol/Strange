package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.screen.mini.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class MiniJournal {
    private final Screen screen;
    private Minecraft minecraft;
    private Font font;
    private ItemRenderer itemRenderer;

    public Section section;
    public Bookmark selectedBookmark;
    public ResourceLocation selectedBiome;
    public ResourceLocation selectedStructure;
    public ResourceLocation selectedDimension;
    public boolean hasRenderedButtons;
    public boolean hasFirstRendered;
    public int offset;
    public int midX;
    public int midY;
    public int journalMidX;

    public final int perPage;
    public final int textColor;
    public final int secondaryColor;
    public final int buttonWidth;
    public final int buttonHeight;

    public static final ResourceLocation CLOSED = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal.png");
    public static final ResourceLocation OPEN = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal_open.png");
    public static final Component INCORRECT_DIMENSION;

    private final Map<Section, BaseMiniScreen> childSections = new HashMap<>();

    public MiniJournal(Screen screen) {
        this.screen = screen;
        this.section = Section.HOME;
        this.offset = 0;
        this.perPage = 6;
        this.textColor = 0x222222;
        this.secondaryColor = 0x908080;
        this.buttonWidth = 88;
        this.buttonHeight = 20;

        this.childSections.put(Section.HOME, new MiniHomeScreen(this));
        this.childSections.put(Section.BOOKMARKS, new MiniBookmarksScreen(this));
        this.childSections.put(Section.DIMENSIONS, new MiniDimensionsScreen(this));
    }

    public void init(Minecraft minecraft) {
        this.minecraft = minecraft;

        midX = screen.width / 2;
        midY = screen.height / 2;
        journalMidX = midX - 88;

        if (screen.height % 2 == 0) {
            midY -= 1;
        }

        hasFirstRendered = false;

        //noinspection Convert2MethodRef
        getChildSection().ifPresent(child -> child.init());
    }

    /**
     * Resolve a section into a child screen using the childSections mapping.
     */
    public Optional<BaseMiniScreen> getChildSection() {
        return Optional.ofNullable(childSections.get(section));
    }

    public Screen getScreen() {
        return screen;
    }

    public Minecraft getMinecraft() {
        return minecraft;
    }

    public void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        ResourceLocation texture = section == Section.HOME ? CLOSED : OPEN;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        screen.blit(poseStack, (screen.width / 2) - 284, (screen.height / 2) - 144, 123, 222, 256, 256);
    }

    private void firstRender(PoseStack poseStack) {
        if (!hasFirstRendered) {

            getChildSection().ifPresent(child
                -> child.firstRender(poseStack, font, itemRenderer));

            hasFirstRendered = true;
        }
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY) {
        // We need to set the local references of the font and item renderers to those supplied by screen.
        font = screen.font;
        itemRenderer = screen.itemRenderer;

        firstRender(poseStack);

        getChildSection().ifPresent(child
            -> child.render(poseStack, itemRenderer, font));
    }

    public void changeSection(Section section) {
        this.section = section;
        redraw();
    }

    public void addBackButton(Button.OnPress onPress) {
        int buttonWidth = 74;
        int buttonHeight = 20;
        screen.addRenderableWidget(new Button(journalMidX - 38, midY + 76, buttonWidth, buttonHeight, JournalResources.GO_BACK, onPress));
    }

    public void renderTitle(PoseStack poseStack, Component title, int y) {
        GuiHelper.drawCenteredString(poseStack, font, title, journalMidX, y, textColor);
    }

    public void redraw() {
        ClientHelper.getClient().ifPresent(mc -> screen.init(minecraft, screen.width, screen.height));
    }

    public enum Section implements ICharmEnum {
        HOME,
        BOOKMARKS,
        BIOMES,
        STRUCTURES,
        DIMENSIONS
    }

    static {
        INCORRECT_DIMENSION = new TranslatableComponent("gui.strange.journal.incorrect_dimension");
    }
}
