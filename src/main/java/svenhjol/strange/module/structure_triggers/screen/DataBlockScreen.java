package svenhjol.strange.module.structure_triggers.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.strange.module.structure_triggers.DataBlockEntity;
import svenhjol.strange.module.structure_triggers.StructureTriggers;

import java.util.function.Consumer;

public class DataBlockScreen extends Screen {
    private BlockPos pos;
    private DataBlockEntity data;
    private EditBox metadataBox;
    private int midX;
    private boolean hasRenderedBottomButtons;

    public DataBlockScreen(BlockPos pos, DataBlockEntity data) {
        super(new TranslatableComponent("gui.strange.data_block.title"));
        this.pos = pos;
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null) return;
        midX = width / 2;

        metadataBox = setupInputBox("MetadataBox", 100, 55, 200, data.getMetadata(), t -> data.setMetadata(t));
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        children.add(metadataBox);
        setFocused(metadataBox);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBottomButtons(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Metadata", midX - 100, 40, 0xffffff);
        metadataBox.render(poseStack, mouseX, mouseY, delta);
    }

    private void renderBottomButtons(PoseStack poseStack) {
        if (!hasRenderedBottomButtons) {
            int buttonWidth = 100;
            int buttonHeight = 20;
            int x = midX - (buttonWidth / 2);
            int y = 130;

            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.data_block.save"), b -> save()));
            hasRenderedBottomButtons = true;
        }
    }

    private EditBox setupInputBox(String id, int x, int y, int length, String value, Consumer<String> onInput) {
        EditBox input = new EditBox(font, midX - 100, y, 200, 12, new TextComponent(id));

        input.changeFocus(true);
        input.setCanLoseFocus(false);
        input.setTextColor(-1);
        input.setTextColorUneditable(-1);
        input.setBordered(true);
        input.setMaxLength(128);
        input.setResponder(onInput);
        input.setValue(value);
        input.setEditable(true);

        return input;
    }

    private void save() {
        if (minecraft != null) {
            NetworkHelper.sendPacketToServer(StructureTriggers.MSG_SERVER_UPDATE_DATA_BLOCK, buf -> {
                data.setChanged();
                buf.writeBlockPos(pos);
                buf.writeNbt(data.saveWithoutMetadata());
            });
            minecraft.setScreen(null);
        }
    }
}
