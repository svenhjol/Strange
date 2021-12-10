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

public class DataBlockScreen extends Screen {
    private BlockPos pos;
    private DataBlockEntity data;
    private EditBox editBox;
    private int midX;
    private String metadata;
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

        metadata = data.getMetadata();
        midX = width / 2;
        editBox = new EditBox(font, midX - 150, 40, 300, 12, new TextComponent("EditBox"));
        editBox.changeFocus(true);
        editBox.setCanLoseFocus(false);
        editBox.setTextColor(-1);
        editBox.setTextColorUneditable(-1);
        editBox.setBordered(true);
        editBox.setMaxLength(256);
        editBox.setResponder(t -> metadata = t);
        editBox.setValue(metadata);
        editBox.setEditable(true);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        children.add(editBox);
        setFocused(editBox);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBottomButtons(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        editBox.render(poseStack, mouseX, mouseY, delta);
    }

    private void renderBottomButtons(PoseStack poseStack) {
        if (!hasRenderedBottomButtons) {
            int buttonWidth = 100;
            int buttonHeight = 20;
            int x = midX - (buttonWidth / 2);
            int y = 80;

            addRenderableWidget(new Button(x, y, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.data_block.save"), b -> save()));
            hasRenderedBottomButtons = true;
        }
    }

    private void save() {
        if (minecraft != null) {
            NetworkHelper.sendPacketToServer(StructureTriggers.MSG_SERVER_UPDATE_DATA_BLOCK, buf -> {
                data.setMetadata(metadata);
                data.setChanged();
//                Objects.requireNonNull(minecraft.level).getBlockEntity(pos).setChanged();

                buf.writeBlockPos(pos);
                buf.writeNbt(data.saveWithoutMetadata());
            });
            minecraft.setScreen(null);
        }
    }
}
