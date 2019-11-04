package svenhjol.strange.scrolls.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.scrolls.message.ClientQuestList;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;

public class QuestsCapability implements IQuestsCapability
{
    private static final String CURRENT_QUESTS = "currentQuests";

    private List<IQuest> currentQuests = new ArrayList<>();

    @Override
    public void acceptQuest(PlayerEntity player, IQuest quest)
    {
        if (currentQuests.stream().noneMatch(q -> q.getId().equals(quest.getId()))
            && currentQuests.size() <= Quests.max
        ) {
            currentQuests.add(quest);
            updateCurrentQuests(player);
        }
    }

    public void removeQuest(PlayerEntity player, IQuest quest)
    {
        currentQuests.removeIf(q -> q.getId().equals(quest.getId()));
        updateCurrentQuests(player);
    }

    @Override
    public List<IQuest> getCurrentQuests(PlayerEntity player)
    {
        return currentQuests;
    }

    @Override
    public void updateCurrentQuests(PlayerEntity player)
    {
        PacketHandler.sendTo(new ClientQuestList(getCurrentQuests(player)), (ServerPlayerEntity)player);
    }

    @Override
    public void readNBT(INBT tag)
    {
        currentQuests = readQuestList(tag, CURRENT_QUESTS);
    }

    @Override
    public INBT writeNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        writeQuestList(tag, CURRENT_QUESTS, currentQuests);
        return tag;
    }

    private List<IQuest> readQuestList(INBT tag, String name)
    {
        List<IQuest> list = new ArrayList<>();
        if (tag == null) return list;

        CompoundNBT ctag = (CompoundNBT)tag;
        INBT inbt = ctag.get(name);
        if (inbt == null) return list;

        for (INBT q : (ListNBT)inbt) {
            Quest quest = new Quest();
            quest.fromNBT((CompoundNBT)q);
            list.add(quest);
        }

        return list;
    }

    private void writeQuestList(CompoundNBT tag, String name, List<IQuest> questList)
    {
        ListNBT list = new ListNBT();
        for (IQuest quest : questList) {
            list.add(quest.toNBT());
        }
        tag.put(name, list);
    }
}
