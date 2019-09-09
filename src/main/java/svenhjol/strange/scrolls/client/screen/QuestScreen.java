package svenhjol.strange.scrolls.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import svenhjol.strange.scrolls.client.gui.GatherQuestPanel;
import svenhjol.strange.scrolls.client.gui.HuntQuestPanel;
import svenhjol.strange.scrolls.item.ScrollItem;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.action.Hunt;

public class QuestScreen extends Screen implements IRenderable
{
    private PlayerEntity player;
    private ItemStack stack;
    private IQuest quest;

    public QuestScreen(PlayerEntity player, ItemStack stack)
    {
        super(new StringTextComponent("Quest"));
        this.player = player;
        this.stack = stack;
        this.quest = ScrollItem.getQuest(stack.copy());
    }

    public QuestScreen(PlayerEntity player, IQuest quest)
    {
        super(new StringTextComponent("Quest"));
        this.player = player;
        this.quest = quest;
        this.stack = null;
    }

    @Override
    protected void init()
    {
        //this.minecraft.keyboardListener.enableRepeatEvents(true);

        if (stack != null) {
            this.addButton(new Button((width / 2) - 140, (height / 4) + 120, 80, 20, I18n.format("gui.charm.scrolls.decline"), (button) -> this.decline()));
            this.addButton(new Button((width / 2) - 40, (height / 4) + 120, 80, 20, I18n.format("gui.charm.scrolls.close"), (button) -> this.close()));
            this.addButton(new Button((width / 2) + 60, (height / 4) + 120, 80, 20, I18n.format("gui.charm.scrolls.accept"), (button) -> this.accept()));
        } else {
            this.addButton(new Button((width / 2) - 100, (height / 4) + 120, 80, 20, I18n.format("gui.charm.scrolls.quit"), (button) -> this.decline()));
            this.addButton(new Button((width / 2) + 20, (height / 4) + 120, 80, 20, I18n.format("gui.charm.scrolls.close"), (button) -> this.close()));
        }
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
        this.drawCenteredString(this.font, this.title.getFormattedText(), (width / 2), 40, 0xFFFFFF);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();

        this.drawCenteredString(this.font, quest.getDescription(), (width / 2), 60, 0xFFFFFF);

        Criteria criteria = quest.getCriteria();

        if (!criteria.getActions(Gather.class).isEmpty()) {
            new GatherQuestPanel(quest, 90, (width / 2) - 160,85);
        }
        if (!criteria.getActions(Hunt.class).isEmpty()) {
            new HuntQuestPanel(quest, 90, (width / 2) + 60, 85);
        }

        GlStateManager.popMatrix();
        super.render(mouseX, mouseY, partialTicks);
    }

    private void accept()
    {
        // TODO message to play accept sound
        if (stack != null) {
            Quests.getCapability(player).acceptQuest(player, quest);
            stack.shrink(1);
        }
        this.close();
    }

    private void decline()
    {
        // TODO message to play decline sound
        if (stack != null) {
            stack.shrink(1);
        } else {
            Quests.getCapability(player).removeQuest(player, quest);
        }
        this.close();
    }

    private void close()
    {
        this.minecraft.displayGuiScreen(null);
    }
}
