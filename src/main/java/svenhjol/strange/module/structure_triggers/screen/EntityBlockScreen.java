package svenhjol.strange.module.structure_triggers.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.strange.module.structure_triggers.EntityBlockEntity;
import svenhjol.strange.module.structure_triggers.StructureTriggers;

public class EntityBlockScreen extends BaseScreen<EntityBlockEntity> {
    private EditBox entityEditBox;
    private EditBox healthEditBox;
    private EditBox countEditBox;
    private EditBox effectsEditBox;
    private EditBox armorEditBox;
    private EditBox metaEditBox;
    private EditBox rotationEditBox;
    private boolean primed;
    private boolean persist;

    public EntityBlockScreen(BlockPos pos, EntityBlockEntity blockEntity) {
        super(pos, blockEntity, new TranslatableComponent("gui.strange.entity_block.title"));
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null) return;

        entityEditBox = setupInputBox("EntityEditBox", 100, 50, 200, blockEntity.getEntity().toString(), t -> blockEntity.setEntity(new ResourceLocation(t)));
        children.add(entityEditBox);

        healthEditBox = setupInputBox("HealthEditBox", 100, 75, 200, String.valueOf(blockEntity.getHealth()), t -> blockEntity.setHealth(Double.parseDouble(t)));
        children.add(healthEditBox);

        countEditBox = setupInputBox("CountEditBox", 100, 90, 200, String.valueOf(blockEntity.getCount()), t -> blockEntity.setCount(Integer.parseInt(t)));
        children.add(countEditBox);

        effectsEditBox = setupInputBox("EffectsEditBox", 100, 105, 200, blockEntity.getEffects(), t -> blockEntity.setEffects(t));
        children.add(effectsEditBox);

        armorEditBox = setupInputBox("ArmorEditBox", 100, 120, 200, blockEntity.getArmor(), t -> blockEntity.setArmor(t));
        children.add(armorEditBox);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        setFocused(entityEditBox);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Entity", midX - 100, 40, 0xffffff);
        entityEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Health", midX - 100, 65, 0xffffff);
        healthEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Count", midX - 100, 80, 0xffffff);
        countEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Effects", midX - 100, 95, 0xffffff);
        effectsEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Armor", midX - 100, 110, 0xffffff);
        armorEditBox.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    protected void save() {
        if (minecraft != null) {
            NetworkHelper.sendPacketToServer(StructureTriggers.MSG_SERVER_UPDATE_BLOCK_ENTITY, buf -> {
                blockEntity.setChanged();
                buf.writeBlockPos(pos);
                buf.writeNbt(blockEntity.saveWithoutMetadata());
            });
            minecraft.setScreen(null);
        }
    }
}
