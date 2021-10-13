package svenhjol.strange.module.runestones;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.JournalsData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;

public class RunestoneScreen extends AbstractContainerScreen<RunestoneMenu> {
    private final ResourceLocation ILLAGER_GLYPHS = new ResourceLocation("minecraft", "illageralt");
    private final Style ILLAGER_GLYPHS_STYLE = Style.EMPTY.withFont(ILLAGER_GLYPHS);
    private IRunestoneMaterial material;
    private RunestoneBlockEntity runestone;
    private ResourceLocation texture;
    private JournalsData playerData = null;
    private int journalCheckTicks = 0;

    public RunestoneScreen(RunestoneMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        passEvents = false;

        // ask server to update the player journal
        JournalsClient.sendSyncJournal();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);

        ClientHelper.getPlayer().ifPresent(player -> {
            renderRunes(poseStack, player);
        });
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getBackgroundTexture());
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);




//        Slot slot = menu.getSlot(0);
//        if (!slot.hasItem()) {
//            return;
//        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // nah
    }

    protected void renderRunes(PoseStack poseStack, Player player) {
        int mid = width / 2;
        boolean isCreative = player.isCreative();

        if (journalCheckTicks++ >= 20) {
            // get reference to cached player data
            JournalsClient.getPlayerData().ifPresent(data -> this.playerData = data);
        }

        if (playerData == null) {
            return;
        }

        if (RunestonesClient.activeDestination == null) {
            return;
        }

        if (this.material == null) {
            this.material = menu.getMaterial();
        }

        String runeString = RunestonesClient.activeDestination.runes;
        String knownRuneString = KnowledgeHelper.convertStringWithLearnedRunes(runeString, playerData);

        int left = mid - 76;
        int top = 76;
        int xOffset = 11;
        int yOffset = 14;
        int index = 0;

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 14; x++) {
                if (index < knownRuneString.length()) {
                    Component rune;
                    int color;

                    String s = String.valueOf(knownRuneString.charAt(index));
                    if (s.equals(KnowledgeHelper.UNKNOWN)) {
                        rune = new TextComponent("?");
                        color = 0xAA0000;
                    } else {
                        rune = new TextComponent(s).withStyle(ILLAGER_GLYPHS_STYLE);
                        color = 0xFFFFFF;
                    }

                    font.drawShadow(poseStack, rune, left + (x * xOffset), top + (y * yOffset), color);
                }
                index++;
            }
        }
    }

    protected ResourceLocation getBackgroundTexture() {
        if (this.texture == null) {
            this.material = menu.getMaterial();
            this.texture = new ResourceLocation(Strange.MOD_ID, "textures/gui/" + material.getSerializedName() + "_runestone.png");
        }

        return this.texture;
    }

    @Override
    public void onClose() {
        RunestonesClient.activeDestination = null;
        super.onClose();
    }
}
