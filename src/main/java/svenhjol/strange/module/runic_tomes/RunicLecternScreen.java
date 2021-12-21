package svenhjol.strange.module.runic_tomes;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.runes.client.RuneStringRenderer;
import svenhjol.strange.module.runestones.RunestoneItemTooltip;
import svenhjol.strange.module.runestones.helper.RunestoneHelper;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class RunicLecternScreen extends AbstractContainerScreen<RunicLecternMenu> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Strange.MOD_ID, "textures/gui/runic_lectern.png");

    private int midX;
    private int midY;

    private Button doneButton;
    private Button takeButton;
    private Button activateButton;

    private ItemStack tome;
    private String runes;
    private Item requiredItem;
    private ResourceLocation dimension;
    private RuneStringRenderer runeStringRenderer;
    private boolean hasProvidedItem;

    public RunicLecternScreen(RunicLecternMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);

        this.passEvents = false;
        this.imageWidth = 176;
        this.imageHeight = 174;
    }

    @Override
    protected void init() {
        super.init();

        midX = width / 2;
        midY = height / 2;

        runeStringRenderer = new RuneStringRenderer(midX - 48, midY - 62, 10, 13, 10, 4);
        dimension = minecraft.level.dimension().location();

        int buttonWidth = 90;

        doneButton = addRenderableWidget(new Button(midX - 140, midY + 94, buttonWidth, 20, CommonComponents.GUI_DONE, button -> {
            onClose();
        }));
        takeButton = addRenderableWidget(new Button(midX - 45, midY + 94, buttonWidth, 20, new TranslatableComponent("gui.strange.runic_lecterns.take_tome"), button -> {
            syncClickedButton(0);
            onClose();
        }));
        activateButton = addRenderableWidget(new Button(midX + 49, midY + 94, buttonWidth, 20, new TranslatableComponent("gui.strange.runic_lecterns.activate"), button -> {
            syncClickedButton(1);
            onClose();
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        hasProvidedItem = menu.slots.get(0).hasItem();

        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);

        // if tome has been sent from the server, update screen props
        if (RunicTomesClient.tomeHolder == null) {
            return;
        }

        if (tome == null) {
            tome = RunicTomesClient.tomeHolder.copy();
            runes = RunicTomeItem.getRunes(tome);
            requiredItem = RunestoneHelper.getItem(dimension, runes);
        }

        runeStringRenderer.render(poseStack, font, runes);

        // if there is a sacrificial item, allow the activate button to be clicked
        activateButton.active = hasProvidedItem;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        setupTextureShaders();
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        if (tome != null && tome.hasCustomHoverName()) {
            font.draw(poseStack, tome.getHoverName(), (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
        }
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        int top = midY - 28;
        int left = midX - 8;
        int bottom = midY - 12;
        int right = midX + 8;

        if (!hasProvidedItem && mouseX > left && mouseX < right && mouseY > top && mouseY < bottom) {
            if (requiredItem == null) return;

            // If player hasn't learned enough runes then exit early.
            int unknown = JournalHelper.countUnknownRunes(runes);
            if (unknown > 0) return;

            List<Component> text = Lists.newArrayList();
            NonNullList<ItemStack> items = NonNullList.create();
            ItemStack stack = new ItemStack(requiredItem);
            items.add(stack);

            text.add(new TranslatableComponent("gui.strange.clues.required_item"));
            text.add(requiredItem.getName(stack));
            renderTooltip(poseStack, text, Optional.of(new RunestoneItemTooltip(items)), mouseX, mouseY);

            return;
        }

        super.renderTooltip(poseStack, mouseX, mouseY);
    }

    private void setupTextureShaders() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    private void syncClickedButton(int r) {
        ClientHelper.getClient().ifPresent(mc -> mc.gameMode.handleInventoryButtonClick((this.menu).containerId, r));
    }
}
