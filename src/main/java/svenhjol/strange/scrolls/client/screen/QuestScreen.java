package svenhjol.strange.scrolls.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import svenhjol.strange.scrolls.client.gui.GatherPanel;
import svenhjol.strange.scrolls.client.gui.HuntPanel;
import svenhjol.strange.scrolls.client.gui.LimitsPanel;
import svenhjol.strange.scrolls.client.gui.RewardsPanel;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.item.ScrollItem;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.action.Hunt;
import svenhjol.strange.scrolls.quest.iface.IQuest;

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
        int mid = (width / 2);

        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), mid, 40, 0xFFFFFF);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();

        this.drawCenteredString(this.font, quest.getDescription(), mid, 60, 0xFFFFFF);

        Criteria criteria = quest.getCriteria();
        int panelWidth = 85;

        if (!criteria.getActions(Gather.class).isEmpty()) {
            new GatherPanel(quest, width / 4, 85, panelWidth);
        }
        if (!criteria.getActions(Hunt.class).isEmpty()) {
            new HuntPanel(quest, 3 * (width / 4), 85, panelWidth);
        }
        if (!criteria.getLimits().isEmpty()) {
            new LimitsPanel(quest, width / 4, 145, panelWidth);
        }
        if (!criteria.getRewards().isEmpty()) {
            new RewardsPanel(quest, 3 * (width / 4), 145, panelWidth);
        }

        GlStateManager.popMatrix();
        super.render(mouseX, mouseY, partialTicks);
    }

    private void accept()
    {
        if (stack != null) {
            MinecraftForge.EVENT_BUS.post(new QuestEvent.Accept(player, quest));
            stack.shrink(1);
        }
        this.close();
    }

    private void decline()
    {
        if (stack != null) {
            stack.shrink(1);
        } else {
            MinecraftForge.EVENT_BUS.post(new QuestEvent.Decline(player, quest));
        }
        this.close();
    }

    private void close()
    {
        this.minecraft.displayGuiScreen(null);
    }
}
