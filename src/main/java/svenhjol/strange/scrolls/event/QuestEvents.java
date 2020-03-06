package svenhjol.strange.scrolls.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsProvider;
import svenhjol.strange.scrolls.message.ClientQuestAction;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.condition.Encounter;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.iface.IQuest.State;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Handles server-side forge events related to quests.
 *
 * If you want your quest delegate to be able to respond to a specific type of Forge event,
 * you must subscribe to it here and call `respondToEvent()`
 */
@SuppressWarnings("unused")
public class QuestEvents
{
    @SubscribeEvent
    public void onQuestAccept(QuestEvent.Accept event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        Quests.getCapability(player).acceptQuest(player, quest);

        if (respondToEvent(player, event)) {
            quest.setState(State.Started);

            Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientQuestAction(ClientQuestAction.ACCEPTED, quest), (ServerPlayerEntity)player);
        } else {
            event.setCanceled(true);
            Quests.getCapability(player).removeQuest(player, quest);
        }
    }

    @SubscribeEvent
    public void onQuestComplete(QuestEvent.Complete event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        respondToEvent(player, event);
        Quests.getCapability(player).removeQuest(player, quest);
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientQuestAction(ClientQuestAction.COMPLETED, quest), (ServerPlayerEntity)player);
    }

    @SubscribeEvent
    public void onQuestDecline(QuestEvent.Decline event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        respondToEvent(player, event);
        Quests.getCapability(player).removeQuest(player, quest);
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientQuestAction(ClientQuestAction.DECLINED, quest), (ServerPlayerEntity)player);
    }

    @SubscribeEvent
    public void onQuestFail(QuestEvent.Fail event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        respondToEvent(player, event);
        Quests.getCapability(player).removeQuest(player, quest);
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientQuestAction(ClientQuestAction.FAILED, quest), (ServerPlayerEntity)player);
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
        final PlayerEntity player = event.getPlayer();

        player.getPersistentData().put(
            Quests.QUESTS_CAP_ID.toString(),
            Quests.getCapability(player).writeNBT());
    }

    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event)
    {
        final PlayerEntity player = event.getPlayer();

        Quests.getCapability(player).readNBT(
            player.getPersistentData()
                .get(Quests.QUESTS_CAP_ID.toString()));
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        final PlayerEntity player = event.getPlayer();

        Quests.getCapability(player).readNBT(
            player.getPersistentData()
                .get(Quests.QUESTS_CAP_ID.toString()));
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        final PlayerEntity player = event.getPlayer();

        player.getPersistentData().put(
            Quests.QUESTS_CAP_ID.toString(),
            Quests.getCapability(player).writeNBT());
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath()) return;
        IQuestsCapability oldCap = Quests.getCapability(event.getOriginal());
        IQuestsCapability newCap = Quests.getCapability(event.getPlayer());
        newCap.readNBT(oldCap.writeNBT());

        PlayerEntity player = event.getOriginal();
        if (player != null) {
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
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
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
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
            && event.getEntityLiving() != null
        ) {
            LivingEntity killed = event.getEntityLiving();

            // needed to propagate out to all players in vicinity of encounter
            if (killed.getTags().contains(Encounter.ENCOUNTER_TAG)) {
                killed.world.getEntitiesWithinAABB(Entity.class, killed.getBoundingBox().grow(Encounter.FIGHT_RANGE))
                    .stream()
                    .filter(p -> p instanceof PlayerEntity)
                    .forEach(p -> respondToEvent((PlayerEntity)p, event));
            }

            Entity source = event.getSource().getTrueSource();
            if (source instanceof PlayerEntity) {
                respondToEvent((PlayerEntity)source, event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.END
            && event.player != null
            && event.player.world.getGameTime() % Quests.tickInterval == 0
            && event.player.isAlive()
        ) {
            respondToEvent(event.player, event);
        }
    }

    private boolean respondToEvent(PlayerEntity player, Event event)
    {
        if (player == null) return false;
        boolean responded = false;

        ConcurrentLinkedDeque<IQuest> quests = new ConcurrentLinkedDeque<>(Quests.getCurrent(player));

        for (IQuest q : quests) {
            responded = q.respondTo(event, player) || responded;
        }

        if (responded) Quests.update(player);
        return responded;
    }
}
