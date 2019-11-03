package svenhjol.strange.scrolls.event;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsProvider;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.iface.IQuest.State;

import java.util.List;

@SuppressWarnings("unused")
public class QuestEvents
{
    @SubscribeEvent
    public void onQuestAccept(QuestEvent.Accept event)
    {
        event.getQuest().setState(State.Started);
        Quests.proxy.toast(event.getQuest(), I18n.format("event.strange.quests.accepted"));
        Quests.getCapability(event.getPlayer()).acceptQuest(event.getPlayer(), event.getQuest());
        respondToEvent(event.getPlayer(), event);
    }

    @SubscribeEvent
    public void onQuestComplete(QuestEvent.Complete event)
    {
        respondToEvent(event.getPlayer(), event);
        Quests.proxy.toastSuccess(event.getQuest(), I18n.format("event.strange.quests.completed"));
        Quests.getCapability(event.getPlayer()).removeQuest(event.getPlayer(), event.getQuest());
    }

    @SubscribeEvent
    public void onQuestDecline(QuestEvent.Decline event)
    {
        respondToEvent(event.getPlayer(), event);
        Quests.proxy.toast(event.getQuest(), I18n.format("event.strange.quests.declined"));
        Quests.getCapability(event.getPlayer()).removeQuest(event.getPlayer(), event.getQuest());
    }

    @SubscribeEvent
    public void onQuestFail(QuestEvent.Fail event)
    {
        final PlayerEntity player = event.getPlayer();
        respondToEvent(event.getPlayer(), event);
        Quests.proxy.toastFailed(event.getQuest(), I18n.format("event.strange.quests.failed"));
        Quests.getCapability(player).removeQuest(player, event.getQuest());
    }

    @SubscribeEvent
    public void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof PlayerEntity)) return;
        event.addCapability(Quests.QUESTS_CAP_ID, new QuestsProvider()); // Attach cap and provider to Forge's player capabilities. Provider has the implementation.
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event)
    {
        event.getPlayer().getPersistentData().put(
            Quests.QUESTS_CAP_ID.toString(),
            Quests.getCapability(event.getPlayer()).writeNBT());
    }

    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event)
    {
        Quests.getCapability(event.getPlayer()).readNBT(
            event.getPlayer().getPersistentData()
                .get(Quests.QUESTS_CAP_ID.toString()));
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath()) return;
        IQuestsCapability oldCap = Quests.getCapability(event.getOriginal());
        IQuestsCapability newCap = Quests.getCapability(event.getPlayer());
        newCap.readNBT(oldCap.writeNBT());

        PlayerEntity player = event.getPlayer();
        if (player != null) {
            respondToEvent(player, event);
        }
    }


    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event)
    {
        PlayerEntity player = event.getPlayer();
        if (player != null) {
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onItemCrafted(ItemCraftedEvent event)
    {
        PlayerEntity player = event.getPlayer();
        if (player != null) {
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof PlayerEntity)
            && event.getSource().getTrueSource() instanceof PlayerEntity
            && event.getEntityLiving() != null
        ) {
            PlayerEntity player = (PlayerEntity)event.getSource().getTrueSource();
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.END
            && event.player != null
            && event.player.world.getGameTime() % 10 == 0
            && event.player.isAlive()
        ) {
            respondToEvent(event.player, event);
        }
    }

//    @SubscribeEvent
//    public void onWorldLoad(WorldEvent.Load event)
//    {
//        IResourceManager rm = Minecraft.getInstance().getResourceManager();
//
//        try {
//            for (int tier = 1; tier < 4; tier++) {
//                Quests.available.put(tier, new ArrayList<>());
//                Collection<ResourceLocation> resources = rm.getAllResourceLocations("quests/tier" + tier, file -> file.endsWith(".json"));
//
//                for (ResourceLocation res : resources) {
//                    IResource resource = rm.getResource(res);
//                    Definition definition = Definition.deserialize(resource);
//                    Quests.available.get(tier).add(definition);
//                }
//            }
//        } catch (Exception e) {
//            Meson.warn("Could not load quests", e);
//        }
//    }

    private void respondToEvent(PlayerEntity player, Event event)
    {
        if (player == null || !player.isAlive()) return;

        List<IQuest> quests = Quests.getCurrent(player);
        boolean responded = false;

        for (IQuest quest : quests) {
            responded = quest.respondTo(event) || responded;
        }

        if (responded) Quests.update(player);
    }
}
