package svenhjol.strange.scrolls.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsProvider;
import svenhjol.strange.scrolls.client.gui.QuestBadgeGui;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.base.message.RequestCurrentQuests;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.IQuest;

import java.util.ArrayList;
import java.util.List;

public class QuestEvents
{
    private List<QuestBadgeGui> questBadges = new ArrayList<>();

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
            if (QuestClient.lastQuery + 20 < mc.world.getGameTime()) {
                PacketHandler.sendToServer(new RequestCurrentQuests());
            }

            int xPos = mc.mainWindow.getScaledWidth() - 122 - 22;
            int yPos = (mc.mainWindow.getScaledHeight() / 2) - (166 / 2);

            questBadges.clear();
            for (int i = 0; i < QuestClient.currentQuests.size(); i++) {
                questBadges.add(new QuestBadgeGui(QuestClient.currentQuests.get(i), mc, xPos, yPos + (i * 30)));
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
            List<IQuest> quests = Quests.getCurrent(player);
            boolean responded = false;

            for (IQuest quest : quests) {
                responded = quest.respondTo(event) || responded;
            }

            if (responded) Quests.update(player);
        }
    }

//    @SubscribeEvent
//    public void onMobKilled(LivingDeathEvent event)
//    {
//        if (!(event.getEntity() instanceof PlayerEntity)
//            && event.getSource().getTrueSource() instanceof PlayerEntity
//            && event.getEntityLiving() != null
//        ) {
//            PlayerEntity player = (PlayerEntity)event.getSource().getTrueSource();
//            LivingEntity mob = event.getEntityLiving();
//
////            Quests.getCapability(player).getCurrentQuests(player)
////                .forEach(q -> Quests.handlers.forEach(h -> h.killMob(event, q)));
//        }
//    }
}
