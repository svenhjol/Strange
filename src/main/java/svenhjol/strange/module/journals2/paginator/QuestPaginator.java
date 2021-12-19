package svenhjol.strange.module.journals2.paginator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestsClient;
import svenhjol.strange.module.scrolls.Scrolls;

import java.util.List;
import java.util.function.Consumer;

public class QuestPaginator extends Paginator<Quest> {
    public QuestPaginator(List<Quest> quests) {
        super(quests);
    }

    @Override
    protected Component getItemName(Quest quest) {
        return new TextComponent(QuestsClient.getTitle(quest.getDefinition()));
    }

    @Override
    protected Consumer<Quest> getItemClickAction(Quest quest) {
        return null;
    }

    @Nullable
    @Override
    protected ItemStack getItemIcon(Quest quest) {
        return new ItemStack(Scrolls.SCROLLS.get(quest.getTier()));
    }
}
