package svenhjol.strange.scrolls.client.screen;

import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.message.ServerQuestAction;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class QuestScreen extends ScrollScreen implements IRenderable {
    public QuestScreen(PlayerEntity player, IQuest quest) {
        super(new StringTextComponent(quest.getTitle()));
        this.player = player;
        this.quest = quest;
    }

    public void renderButtons() {
        int y = (height / 4) + 160;
        int w = 80;
        int h = 20;

        this.addButton(new Button((width / 2) - 100, y, w, h, I18n.format("gui.strange.scrolls.quit"), (button) -> this.quit()));
        this.addButton(new Button((width / 2) + 20, y, w, h, I18n.format("gui.strange.scrolls.close"), (button) -> this.close()));
    }

    private void quit() {
        Strange.LOG.debug(Strange.CLIENT, "calling server to decline quest: " + quest.getId());
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerQuestAction(ServerQuestAction.QUIT, quest.getId()));
        this.close();
    }
}
