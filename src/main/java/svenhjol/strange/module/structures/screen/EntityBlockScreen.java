package svenhjol.strange.module.structures.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.strange.module.structures.EntityBlockEntity;
import svenhjol.strange.module.structures.StructuresClient;

public class EntityBlockScreen extends BaseScreen<EntityBlockEntity> {
    private EditBox entityEditBox;
    private EditBox healthEditBox;
    private EditBox countEditBox;
    private EditBox effectsEditBox;
    private EditBox armorEditBox;
    private EditBox metaEditBox;
    private EditBox rotationEditBox;
    private Checkbox persistentCheckbox;
    private Checkbox primedCheckbox;

    private String count;
    private String health;

    public EntityBlockScreen(BlockPos pos, EntityBlockEntity blockEntity) {
        super(pos, blockEntity, new TranslatableComponent("gui.strange.entity_block.title"));
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null) return;

        count = String.valueOf(blockEntity.getCount());
        health = String.valueOf(blockEntity.getHealth());

        entityEditBox = setupInputBox("EntityEditBox", midX - 100, 50, 200, blockEntity.getEntity(), t -> blockEntity.setEntity(t));
        addRenderableWidget(entityEditBox);

        healthEditBox = setupInputBox("HealthEditBox", midX - 100, 75, 200, health, t -> health = t);
        addRenderableWidget(healthEditBox);

        countEditBox = setupInputBox("CountEditBox", midX - 100, 100, 200, count, t -> count = t);
        addRenderableWidget(countEditBox);

        effectsEditBox = setupInputBox("EffectsEditBox", midX - 100, 125, 200, blockEntity.getEffects(), t -> blockEntity.setEffects(t));
        addRenderableWidget(effectsEditBox);

        armorEditBox = setupInputBox("ArmorEditBox", midX - 100, 150, 200, blockEntity.getArmor(), t -> blockEntity.setArmor(t));
        addRenderableWidget(armorEditBox);

        persistentCheckbox = new Checkbox(midX - 100, 166, 150, 20, new TextComponent("Persistent"), blockEntity.isPersistent());
        addRenderableWidget(persistentCheckbox);

        primedCheckbox = new Checkbox(midX - 100, 188, 150, 20, new TextComponent("Primed"), blockEntity.isPrimed());
        addRenderableWidget(primedCheckbox);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Entity", midX - 100, 40, 0xffffff);
        entityEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Health", midX - 100, 65, 0xffffff);
        healthEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Count", midX - 100, 90, 0xffffff);
        countEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Effects", midX - 100, 115, 0xffffff);
        effectsEditBox.render(poseStack, mouseX, mouseY, delta);

        font.draw(poseStack, "Armor", midX - 100, 140, 0xffffff);
        armorEditBox.render(poseStack, mouseX, mouseY, delta);

        persistentCheckbox.render(poseStack, mouseX, mouseY, delta);
        primedCheckbox.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    protected void save() {
        if (minecraft != null) {
            blockEntity.setCount(Integer.parseInt(count));
            blockEntity.setHealth(Double.parseDouble(health));
            blockEntity.setPersistent(persistentCheckbox.selected());
            blockEntity.setPrimed(primedCheckbox.selected());
            blockEntity.setChanged();

            StructuresClient.CLIENT_SEND_UPDATE_STRUCTURE_BLOCK.send(blockEntity, pos);
            minecraft.setScreen(null);
        }
    }
}
