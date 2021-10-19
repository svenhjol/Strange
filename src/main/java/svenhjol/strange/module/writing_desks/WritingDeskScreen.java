package svenhjol.strange.module.writing_desks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeFonts;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("ConstantConditions")
public class WritingDeskScreen extends AbstractContainerScreen<WritingDeskMenu> {
    private int unknownColor = 0x999999;
    private int uninkedColor = 0x888888;
    private int knownColor = 0x000000;
    private int midX = 0;
    private int midY = 0;
    private int runesLeft = -69;
    private int runesTop = -17;
    private int runeInputXOffset = 11;
    private int runeInputYOffset = 14;
    private boolean hasInk = false;
    private boolean hasBook = false;
    private String runes = "";

    public static final ResourceLocation TEXTURE = new ResourceLocation(Strange.MOD_ID, "textures/gui/writing_desk.png");

    public WritingDeskScreen(WritingDeskMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.passEvents = false;
        this.imageWidth = 176;
        this.imageHeight = 210;

        // ask server to update the player journal
        JournalsClient.sendSyncJournal();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        midX = width / 2;
        midY = height / 2;

        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, delta);

        Slot bookSlot = menu.slots.get(0);
        Slot inkSlot = menu.slots.get(1);
        hasBook = bookSlot != null && bookSlot.hasItem();
        hasInk = inkSlot != null && inkSlot.hasItem();

        if (!hasBook) {
            runes = "";
        }

        runForValidPlayer((player, journal) -> {
            renderBookBg(poseStack);
            renderRuneInput(poseStack, journal);
            renderDeleteButton(poseStack);
            renderWrittenRunes(poseStack);
        });
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        setupTextureShaders();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight, 512, 256);
    }

    private void renderBookBg(PoseStack poseStack) {
        if (hasBook) {
            setupTextureShaders();
            blit(poseStack, midX - 56, midY - 96, this.imageWidth, 0, 110, 69, 512, 256);
        }
    }

    private void renderRuneInput(PoseStack poseStack, JournalData journal) {
        List<Integer> learnedRunes = journal.getLearnedRunes();
        int left = midX + runesLeft;
        int top = midY + runesTop;

        int ix = 0;
        int iy = 0;

        for (int r = 0; r < Knowledge.NUM_RUNES; r++) {
            Component rune;
            int color;

            if (learnedRunes.contains(r)) {
                String s = String.valueOf((char)(r + 97));
                rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                color = hasInk ? knownColor : uninkedColor;
            } else {
                rune = new TextComponent(KnowledgeHelper.UNKNOWN);
                color = unknownColor;
            }

            font.draw(poseStack, rune, left + (ix * runeInputXOffset), top + (iy * runeInputYOffset), color);
            ix++;
            if (ix == 13) {
                ix = 0;
                iy++;
            }
        }
    }

    private void renderDeleteButton(PoseStack poseStack) {
        if (hasBook && runes.length() > 0) {
            setupTextureShaders();
            blit(poseStack, midX + 39, midY - 39, this.imageWidth, 69, 8, 7, 512, 256);
        }
    }

    private void renderWrittenRunes(PoseStack poseStack) {
        int left = midX - 48;
        int top = midY - 88;
        int runesXOffset = 10;
        int runesYOffset = 13;

        int ix = 0;
        int iy = 0;
        int color = 0x997755;

        for (int i = 0; i < runes.length(); i++) {
            String s = String.valueOf(runes.charAt(i));
            Component rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
            font.draw(poseStack, rune, left + (ix * runesXOffset), top + (iy * runesYOffset), color);
            ix++;
            if (ix == 10) {
                ix = 0;
                iy++;
            }
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int left = midX + runesLeft;
        int top = midY + runesTop;

        int ix = 0;
        int iy = 0;

        for (int r = 0; r < Knowledge.NUM_RUNES; r++) {
            int sx = left + (ix * runeInputXOffset);
            int sy = top + (iy * runeInputYOffset);

            if (hasInk && x > sx && x < sx + runeInputXOffset && y > sy && y < sy + runeInputYOffset) {
                runeClicked(r);
                return true;
            }

            ix++;
            if (ix == 13) {
                ix = 0;
                iy++;
            }
        }

        if (runes.length() > 0) {
            int deleteLeft = midX + 39;
            int deleteTop = midY - 39;
            if (x >= deleteLeft && x <= deleteLeft + 8 && y >= deleteTop && y <= deleteTop + 7) {
                deleteClicked();
                return true;
            }
        }

        return super.mouseClicked(x, y, button);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        // nope
    }

    private void runeClicked(int rune) {
        runForValidPlayer((player, journal) -> {
            if (journal.getLearnedRunes().contains(rune) && runes.length() <= KnowledgeData.MAX_LENGTH) {
                runes += String.valueOf((char)(rune + 97));
                syncClickedButton(rune);
            }
        });
    }

    private void deleteClicked() {
        runes = runes.substring(0, runes.length() - 1);
        syncClickedButton(WritingDeskMenu.DELETE);
    }

    private void setupTextureShaders() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    private void runForValidPlayer(BiConsumer<Player, JournalData> run) {
        ClientHelper.getPlayer().ifPresent(player -> Journals.getPlayerData(player).ifPresent(journal
            -> run.accept(player, journal)));
    }

    private void syncClickedButton(int r) {
        ClientHelper.getClient().ifPresent(mc -> mc.gameMode.handleInventoryButtonClick((this.menu).containerId, r));
    }
}
