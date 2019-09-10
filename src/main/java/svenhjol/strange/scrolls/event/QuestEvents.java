package svenhjol.strange.scrolls.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.base.message.RequestCurrentQuests;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsProvider;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.client.gui.QuestBadgeGui;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.iface.IQuest.State;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class QuestEvents
{
    private List<QuestBadgeGui> questBadges = new ArrayList<>();

    @SubscribeEvent
    public void onQuestAccept(QuestEvent.Accept event)
    {
        event.getQuest().setState(State.Started);
        Quests.getCapability(event.getPlayer()).acceptQuest(event.getPlayer(), event.getQuest());
        respondToEvent(event.getPlayer(), event);
    }

    @SubscribeEvent
    public void onQuestComplete(QuestEvent.Complete event)
    {
        respondToEvent(event.getPlayer(), event);
        Quests.getCapability(event.getPlayer()).removeQuest(event.getPlayer(), event.getQuest());
    }

    @SubscribeEvent
    public void onQuestDecline(QuestEvent.Decline event)
    {
        respondToEvent(event.getPlayer(), event);
        Quests.getCapability(event.getPlayer()).removeQuest(event.getPlayer(), event.getQuest());
    }

    @SubscribeEvent
    public void onQuestFail(QuestEvent.Fail event)
    {
        final PlayerEntity player = event.getPlayer();
        respondToEvent(event.getPlayer(), event);
        player.sendStatusMessage(new StringTextComponent("YOU SUCK AT THIS"), true);
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
        event.getPlayer().getPersistantData().put(
            Quests.QUESTS_CAP_ID.toString(),
            Quests.getCapability(event.getPlayer()).writeNBT());
    }

    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event)
    {
        Quests.getCapability(event.getPlayer()).readNBT(
            event.getPlayer().getPersistantData()
                .get(Quests.QUESTS_CAP_ID.toString()));
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath()) return;
        IQuestsCapability oldCap = Quests.getCapability(event.getOriginal());
        IQuestsCapability newCap = Quests.getCapability(event.getPlayer());
        newCap.readNBT(oldCap.writeNBT());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreen instanceof InventoryScreen) {
            if (QuestClient.lastQuery + 40 < mc.world.getGameTime()) {
                PacketHandler.sendToServer(new RequestCurrentQuests());
            }

            // TODO should not collide with potion effects
            int xPos = mc.mainWindow.getScaledWidth() - 122 - 22;
            int yPos = (mc.mainWindow.getScaledHeight() / 2) - (166 / 2) + 70;

            questBadges.clear();
            for (int i = 0; i < QuestClient.currentQuests.size(); i++) {
                questBadges.add(new QuestBadgeGui(QuestClient.currentQuests.get(i), xPos, yPos + (i * 36)));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onMouseClicked(GuiScreenEvent.MouseClickedEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreen instanceof InventoryScreen) {
            double x = event.getMouseX();
            double y = event.getMouseY();

            if (event.getButton() == 0) {
                for (QuestBadgeGui badge : questBadges) {
                    if (badge.isInBox(x, y)) badge.onLeftClick();
                }
            }
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
            && event.player.world.getGameTime() % 10 == 0
        ) {
            respondToEvent(event.player, event);
        }
    }

    private boolean respondToEvent(PlayerEntity player, Event event)
    {
        List<IQuest> quests = Quests.getCurrent(player);
        boolean responded = false;

        for (IQuest quest : quests) {
            responded = quest.respondTo(event) || responded;
        }

        if (responded) Quests.update(player);
        return responded;
    }
}
