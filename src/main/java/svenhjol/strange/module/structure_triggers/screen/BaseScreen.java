package svenhjol.strange.module.structure_triggers.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public abstract class BaseScreen <T extends BlockEntity> extends Screen {
    protected T blockEntity;
    protected BlockPos pos;
    protected int midX;
    protected boolean hasRenderedBottomButtons;

    protected BaseScreen(BlockPos pos, T blockEntity, Component component) {
        super(component);
        this.pos = pos;
        this.blockEntity = blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null) return;
        midX = width / 2;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBottomButtons(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
    }

    protected void renderBottomButtons(PoseStack poseStack) {
        if (!hasRenderedBottomButtons) {
            int buttonWidth = 100;
            int buttonHeight = 20;
            int x = midX - (buttonWidth / 2);
            int y = 170;

            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.save_changes"), b -> save()));
            hasRenderedBottomButtons = true;
        }
    }

    protected EditBox setupInputBox(String id, int x, int y, int length, String value, Consumer<String> onInput) {
        EditBox input = new EditBox(font, x, y, length, 12, new TextComponent(id));

        input.changeFocus(false);
        input.setCanLoseFocus(true);
        input.setTextColor(-1);
        input.setTextColorUneditable(-1);
        input.setBordered(true);
        input.setMaxLength(256);
        input.setResponder(onInput);
        input.setValue(value);
        input.setEditable(true);
        input.setFocus(false);

        return input;
    }

    protected abstract void save();
}
