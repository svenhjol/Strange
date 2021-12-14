package svenhjol.strange.module.structures.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.strange.module.structures.DataBlockEntity;
import svenhjol.strange.module.structures.Structures;

public class DataBlockScreen extends BaseScreen<DataBlockEntity> {
    private EditBox metadataEditBox;

    public DataBlockScreen(BlockPos pos, DataBlockEntity blockEntity) {
        super(pos, blockEntity, new TranslatableComponent("gui.strange.data_block.title"));
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null) return;
        midX = width / 2;

        metadataEditBox = setupInputBox("MetadataEditBox", midX - 100, 55, 200, blockEntity.getMetadata(), t -> blockEntity.setMetadata(t));
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        children.add(metadataEditBox);
        setFocused(metadataEditBox);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        font.draw(poseStack, "Metadata", midX - 100, 40, 0xffffff);
        metadataEditBox.render(poseStack, mouseX, mouseY, delta);
    }

    protected void save() {
        if (minecraft != null) {
            NetworkHelper.sendPacketToServer(Structures.MSG_SERVER_UPDATE_BLOCK_ENTITY, buf -> {
                blockEntity.setChanged();
                buf.writeBlockPos(pos);
                buf.writeNbt(blockEntity.saveWithoutMetadata());
            });
            minecraft.setScreen(null);
        }
    }
}