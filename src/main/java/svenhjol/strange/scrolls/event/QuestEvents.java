package svenhjol.strange.scrolls.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.base.message.RequestCurrentQuests;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsProvider;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.client.gui.QuestBadgeGui;
import svenhjol.strange.scrolls.client.toast.QuestToast.Type;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.iface.IQuest.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class QuestEvents
{
    private List<QuestBadgeGui> questBadges = new ArrayList<>();

    @SubscribeEvent
    public void onQuestAccept(QuestEvent.Accept event)
    {
        event.getQuest().setState(State.Started);
        Quests.toast(event.getQuest(), Type.General, I18n.format("event.strange.quests.accepted"));
        Quests.getCapability(event.getPlayer()).acceptQuest(event.getPlayer(), event.getQuest());
        respondToEvent(event.getPlayer(), event);
    }

    @SubscribeEvent
    public void onQuestComplete(QuestEvent.Complete event)
    {
        respondToEvent(event.getPlayer(), event);
        Quests.toast(event.getQuest(), Type.Success, I18n.format("event.strange.quests.completed"));
        Quests.getCapability(event.getPlayer()).removeQuest(event.getPlayer(), event.getQuest());
    }

    @SubscribeEvent
    public void onQuestDecline(QuestEvent.Decline event)
    {
        respondToEvent(event.getPlayer(), event);
        Quests.toast(event.getQuest(), Type.General, I18n.format("event.strange.quests.declined"));
        Quests.getCapability(event.getPlayer()).removeQuest(event.getPlayer(), event.getQuest());
    }

    @SubscribeEvent
    public void onQuestFail(QuestEvent.Fail event)
    {
        final PlayerEntity player = event.getPlayer();
        respondToEvent(event.getPlayer(), event);
        Quests.toast(event.getQuest(), Type.Failed, I18n.format("event.strange.quests.failed"));
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        int delayTicks = 100;

        if (isValidQuestBadgeScreen(mc)) {
            if (QuestClient.lastQuery + delayTicks < mc.world.getGameTime()) {
                PacketHandler.sendToServer(new RequestCurrentQuests());
                QuestClient.lastQuery = mc.world.getGameTime();
            }

            int w = QuestBadgeGui.WIDTH;
            int numQuests = QuestClient.currentQuests.size();
            if (numQuests == 0) return;

            int xPos = (mc.mainWindow.getScaledWidth() / 2) - (numQuests*w/2);
            int yPos = (mc.mainWindow.getScaledHeight() / 4) - 50;

            questBadges.clear();
            for (int i = 0; i < numQuests; i++) {
                questBadges.add(new QuestBadgeGui(QuestClient.currentQuests.get(i), xPos + (i * (w + 10)), yPos));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onMouseClicked(GuiScreenEvent.MouseClickedEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (isValidQuestBadgeScreen(mc)) {
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

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        IResourceManager rm = Minecraft.getInstance().getResourceManager();

        try {
            for (int tier = 1; tier < 4; tier++) {
                Quests.available.put(tier, new ArrayList<>());
                Collection<ResourceLocation> resources = rm.getAllResourceLocations("quests/tier" + tier, file -> file.endsWith(".json"));

                for (ResourceLocation res : resources) {
                    IResource resource = rm.getResource(res);
                    Definition definition = Definition.deserialize(resource);
                    Quests.available.get(tier).add(definition);
                }
            }
        } catch (Exception e) {
            Meson.warn("Could not load quests", e);
        }
    }

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

    private boolean isValidQuestBadgeScreen(Minecraft mc)
    {
        return mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof CreativeScreen;
    }
}
