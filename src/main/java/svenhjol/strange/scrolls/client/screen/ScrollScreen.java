package svenhjol.strange.scrolls.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import svenhjol.strange.scrolls.client.gui.QuestGatherGui;
import svenhjol.strange.scrolls.item.ScrollItem;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.IQuest;

public class ScrollScreen extends Screen implements IRenderable
{
    private PlayerEntity player;
    private ItemStack stack;
    private IQuest quest;

    public ScrollScreen(PlayerEntity player, ItemStack stack)
    {
        super(new StringTextComponent("Quest"));
        this.player = player;
        this.stack = stack;
        this.quest = ScrollItem.getQuest(stack.copy());
    }

    @Override
    protected void init()
    {
        //this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.addButton(new Button(this.width / 2 - 140, this.height / 4 + 120, 80, 20, I18n.format("gui.charm.scrolls.decline"), (button) -> this.decline()));
        this.addButton(new Button(this.width / 2 - 40, this.height / 4 + 120, 80, 20, I18n.format("gui.charm.scrolls.close"), (button) -> this.close()));
        this.addButton(new Button(this.width / 2 + 60, this.height / 4 + 120, 80, 20, I18n.format("gui.charm.scrolls.accept"), (button) -> this.accept()));
    }

    @Override
    public void onClose()
    {
        this.close();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 40, 16777215);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();

        this.drawCenteredString(this.font, quest.getDescription(), this.width / 2, 60, 16777215);

        if (quest.getType().equals(Quests.QuestType.Gathering)) {
            new QuestGatherGui(quest, Minecraft.getInstance(), this.width);
        }

//        if (outputs != null) {
//            this.drawRightAlignedString(this.font, "You get:", (this.width / 2) - 40, 105, 16777215);
//            for (int i = 0; i < outputs.size(); i++) {
//                ItemStack stack = ItemStack.read((CompoundNBT) outputs.get(i));
//                this.itemRenderer.renderItemIntoGUI(stack, (this.width / 2) + (i * 50), 100);
//                this.drawString(this.font, " x" + stack.getCount(), (this.width / 2) + (i * 50) + 14, 105, 16777215);
//            }
//        }

//        this.drawCenteredString(this.font, str, this.width / 2, 70, 16777215);

//        GlStateManager.translatef((float)(this.width / 2), 0.0F, 50.0F);
//        float f = 93.75F;
//        GlStateManager.scalef(-93.75F, -93.75F, -93.75F);
//        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.popMatrix();
        super.render(mouseX, mouseY, partialTicks);
    }

    private void accept()
    {
        // TODO message to play accept sound
        Quests.getCapability(player).acceptQuest(player, quest);
        stack.shrink(1);
        this.close();
    }

    private void decline()
    {
        // TODO message to play decline sound
        stack.shrink(1);
        this.close();
    }

    private void close()
    {
        this.minecraft.displayGuiScreen(null);
    }
}
