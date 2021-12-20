package svenhjol.strange.module.writing_desks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeFonts;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.helper.Journal2Helper;
import svenhjol.strange.module.journals2.screen.MiniJournalScreen;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class WritingDeskScreen extends AbstractContainerScreen<WritingDeskMenu> {
    public static final ResourceLocation TEXTURE;
    public static final Component DELETE;

    private final int unknownColor;
    private final int uninkedColor;
    private final int knownColor;
    private final int bookKnownColor;
    private final int bookUnknownColor;
    private final int inputRunesLeft;
    private final int inputRunesTop;
    private final int inputRunesXOffset;
    private final int inputRunesYOffset;
    private final int inputRunesWrapAt;
    private final int deleteButtonLeft;
    private final int deleteButtonTop;
    private final int deleteButtonWidth;
    private final int deleteButtonHeight;
    private final int deleteButtonYOffset;
    private boolean hasInk = false;
    private boolean hasBook = false;

    private String runes = "";
    private int midX;
    private int midY;

    private MiniJournalScreen miniJournal;

    public WritingDeskScreen(WritingDeskMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.passEvents = false;
        this.imageWidth = 300;
        this.imageHeight = 221;
        this.inputRunesLeft = -8;
        this.inputRunesTop = -12;
        this.inputRunesXOffset = 11;
        this.inputRunesYOffset = 14;
        this.inputRunesWrapAt = 13;
        this.unknownColor = 0x999999;
        this.uninkedColor = 0x888888;
        this.knownColor = 0x000000;
        this.bookKnownColor = 0x997755;
        this.bookUnknownColor = 0xC0B0A0;
        this.deleteButtonLeft = 102;
        this.deleteButtonTop = -39;
        this.deleteButtonWidth = 5;
        this.deleteButtonHeight = 8;
        this.deleteButtonYOffset = 69;
        this.midX = 0;
        this.midY = 0;
    }

    @Override
    protected void init() {
        super.init();

        miniJournal = new MiniJournalScreen(minecraft, this);
        miniJournal.init();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        midX = width / 2;
        midY = height / 2;

        if (height % 2 == 0) {
            midY -= 1;
        }

        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, delta);

        Slot bookSlot = menu.slots.get(0);
        Slot inkSlot = menu.slots.get(1);
        hasBook = bookSlot != null && bookSlot.hasItem();
        hasInk = inkSlot != null && inkSlot.hasItem();

        if (!hasBook || !hasInk) {
            runes = "";
        }

        renderBookBg(poseStack);
        renderInputRunes(poseStack);
        renderDeleteButton(poseStack, mouseX, mouseY);
        renderWrittenRunes(poseStack);
        renderTooltip(poseStack, mouseX, mouseY);
        miniJournal.render(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        setupTextureShaders();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight, 512, 256);

        miniJournal.renderBg(poseStack, delta, mouseX, mouseY);
    }

    private void renderBookBg(PoseStack poseStack) {
        if (hasBook) {
            setupTextureShaders();
            blit(poseStack, midX + 7, midY - 91, this.imageWidth, 0, 111, 69, 512, 256);
        }
    }

    private void renderInputRunes(PoseStack poseStack) {
        var journal = Journals2Client.journal;
        if (journal == null) return;

        List<Integer> learnedRunes = journal.getLearnedRunes();
        int left = midX + inputRunesLeft;
        int top = midY + inputRunesTop;

        int ix = 0;
        int iy = 0;

        for (int rune = 0; rune < Runes.NUM_RUNES; rune++) {
            Component runeText;
            int color;

            if (learnedRunes.contains(rune)) {
                String s = String.valueOf((char)(rune + Runes.FIRST_RUNE));
                runeText = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                color = hasInk ? knownColor : uninkedColor;
            } else {
                runeText = new TextComponent(String.valueOf(Runes.UNKNOWN_RUNE));
                color = unknownColor;
            }

            font.draw(poseStack, runeText, left + (ix * inputRunesXOffset), top + (iy * inputRunesYOffset), color);
            ix++;
            if (ix == inputRunesWrapAt) {
                ix = 0;
                iy++;
            }
        }
    }

    private void renderDeleteButton(PoseStack poseStack, int mouseX, int mouseY) {
        if (hasBook && runes.length() > 0) {
            int left = midX + deleteButtonLeft;
            int top = midY + deleteButtonTop;
            setupTextureShaders();
            blit(poseStack, left, top, imageWidth, deleteButtonYOffset, deleteButtonWidth, deleteButtonHeight, 512, 256);

            if (mouseX > left && mouseX < left + deleteButtonWidth && mouseY > top && mouseY < top + deleteButtonHeight) {
                List<Component> hoverText = List.of(DELETE);
                renderTooltip(poseStack, hoverText, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private void renderWrittenRunes(PoseStack poseStack) {
        renderRunesString(
            poseStack,
            runes,
            midX + 15,
            midY - 85,
            10,
            13,
            10,
            4,
            false
        );
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int left = midX + inputRunesLeft;
        int top = midY + inputRunesTop;

        int ix = 0;
        int iy = 0;

        for (int rune = 0; rune < Runes.NUM_RUNES; rune++) {
            int sx = left + (ix * inputRunesXOffset);
            int sy = top + (iy * inputRunesYOffset);

            if (hasBook && hasInk && x > sx && x < sx + inputRunesXOffset && y > sy && y < sy + inputRunesYOffset) {
                runeClicked(rune);
                return true;
            }

            ix++;
            if (ix == inputRunesWrapAt) {
                ix = 0;
                iy++;
            }
        }

        if (runes.length() > 0) {
            int deleteLeft = midX + deleteButtonLeft;
            int deleteTop = midY + deleteButtonTop;
            if (x >= deleteLeft && x <= deleteLeft + deleteButtonWidth && y >= deleteTop && y <= deleteTop + deleteButtonHeight) {
                deleteClicked();
                return true;
            }
        }

        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean keyPressed(int code, int j, int k) {
        if (hasBook && hasInk) {
            if (code >= 65 && code <= 90) {
                runeClicked(code - 65);
                return true;
            } else if (code == 259) {
                deleteClicked();
                return true;
            }
        }
        return super.keyPressed(code, j, k);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        font.draw(poseStack, this.title, 132, (float) this.titleLabelY, 4210752);
    }

    private void runeClicked(int rune) {
        var journal = Journals2Client.journal;
        if (journal == null) return;

        if (journal.getLearnedRunes().contains(rune) && runes.length() <= Runes.MAX_PHRASE_LENGTH) {
            runes += String.valueOf((char)(rune + Runes.FIRST_RUNE));
            syncClickedButton(rune);
        }
    }

    private void deleteClicked() {
        if (runes.length() > 0) {
            runes = runes.substring(0, runes.length() - 1);
            syncClickedButton(WritingDeskMenu.DELETE);
        }
    }

    private void setupTextureShaders() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    private void syncClickedButton(int r) {
        ClientHelper.getClient().ifPresent(mc -> mc.gameMode.handleInventoryButtonClick((this.menu).containerId, r));
    }

    private void renderRunesString(PoseStack poseStack, String runes, int left, int top, int xOffset, int yOffset, int xMax, int yMax, boolean withShadow) {
        if (minecraft == null) return;

        // Convert the input string according to the runes that the player knows.
        String revealed = RuneHelper.revealRunes(runes, Journal2Helper.getLearnedRunes());

        int index = 0;

        for (int y = 0; y < yMax; y++) {
            for (int x = 0; x < xMax; x++) {
                if (index < revealed.length()) {
                    Component rune;
                    int color;

                    var unknown = String.valueOf(Runes.UNKNOWN_RUNE);
                    String s = String.valueOf(revealed.charAt(index));
                    if (s.equals(unknown)) {
                        rune = new TextComponent(unknown);
                        color = unknownColor;
                    } else {
                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                        color = knownColor;
                    }

                    int xo = left + (x * xOffset);
                    int yo = top + (y * yOffset);

                    if (withShadow) {
                        minecraft.font.drawShadow(poseStack, rune, xo, yo, color);
                    } else {
                        minecraft.font.draw(poseStack, rune, xo, yo, color);
                    }
                }
                index++;
            }
        }
    }

    static {
        DELETE = new TranslatableComponent("gui.strange.writing_desks.delete");
        TEXTURE = new ResourceLocation(Strange.MOD_ID, "textures/gui/writing_desk.png");
    }
}
