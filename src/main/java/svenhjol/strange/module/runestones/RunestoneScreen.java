package svenhjol.strange.module.runestones;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;

public class RunestoneScreen extends AbstractContainerScreen<RunestoneMenu> {
    private final ResourceLocation ILLAGER_GLYPHS = new ResourceLocation("minecraft", "illageralt");
    private final Style ILLAGER_GLYPHS_STYLE = Style.EMPTY.withFont(ILLAGER_GLYPHS);
    private IRunestoneMaterial material;
    private ResourceLocation texture;

    public RunestoneScreen(RunestoneMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
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

        Slot slot = menu.getSlot(0);
        if (!slot.hasItem()) {
            return;
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        // nah
    }

    protected ResourceLocation getBackgroundTexture() {
        if (this.texture == null) {
            this.material = menu.getMaterial();
            this.texture = new ResourceLocation(Strange.MOD_ID, "textures/gui/" + material.getSerializedName() + "_runestone.png");
        }

        return this.texture;
    }
}
