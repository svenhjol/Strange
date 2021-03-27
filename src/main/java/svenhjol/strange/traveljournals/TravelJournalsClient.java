package svenhjol.strange.traveljournals;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.traveljournals.gui.TravelJournalScreen;
import svenhjol.strange.traveljournals.gui.UpdateEntryScreen;

import java.util.ArrayList;
import java.util.List;

public class TravelJournalsClient extends CharmClientModule {
    public static List<JournalEntry> entries = new ArrayList<>();

    public static JournalEntry entryHavingScreenshot;
    public static int screenshotTicks;
    public static int lastPage;

    public TravelJournalsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void init() {
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);

        ClientPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_CLIENT_RECEIVE_ENTRIES, this::handleClientReceiveEntries);
        ClientPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_CLIENT_RECEIVE_ENTRY, this::handleClientReceiveEntry);
    }

    public static void closeIfNotHolding(MinecraftClient client) {
        if (client != null) {
            if (client.player != null) {
                if (client.player.getMainHandStack().getItem() == TravelJournals.TRAVEL_JOURNAL
                    || client.player.getOffHandStack().getItem() == TravelJournals.TRAVEL_JOURNAL) {
                    return; // don't close the screen if holding a travel journal
                }
            }

            client.openScreen(null);
        }
    }

    public static boolean isPlayerAtEntryPosition(PlayerEntity player, JournalEntry entry) {
        return entry.pos != null && PosHelper.getDistanceSquared(player.getBlockPos(), entry.pos) < TravelJournals.SCREENSHOT_DISTANCE;
    }

    private void handlePlayerTick(PlayerEntity player) {
        if (!player.world.isClient)
            return;

        if (screenshotTicks > 0) {
            if (entryHavingScreenshot == null) {
                screenshotTicks = 0;
            } else if (++screenshotTicks > 30) {
                MinecraftClient client = MinecraftClient.getInstance();
                Window win = client.getWindow();

                ScreenshotUtils.saveScreenshot(
                    client.runDirectory,
                    entryHavingScreenshot.id + ".png",
                    win.getFramebufferWidth() / 8,
                    win.getFramebufferHeight() / 8,
                    client.getFramebuffer(),
                    i -> {
                        client.player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
                        client.options.hudHidden = false;
                        client.execute(() -> {
                            client.openScreen(new UpdateEntryScreen(entryHavingScreenshot));
                            entryHavingScreenshot = null;
                        });
                        Charm.LOG.debug("Screenshot taken");
                    }
                );
                screenshotTicks = 0;
            }
        }
    }

    private void handleClientReceiveEntries(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        CompoundTag tag = data.readCompoundTag();
        if (tag == null)
            return;

        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null)
                return;

            String uuid = player.getUuidAsString();
            Tag entriesTag = tag.get(uuid);
            if (entriesTag == null)
                return;

            entries = TravelJournalsHelper.getEntriesFromListTag((ListTag)entriesTag);
            client.openScreen(new TravelJournalScreen());
        });
    }

    private void handleClientReceiveEntry(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        CompoundTag tag = data.readCompoundTag();
        if (tag == null)
            return;

        client.execute(() -> {
            JournalEntry entry = new JournalEntry(tag);

            if (client.player != null)
                client.player.playSound(SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN, 1.0F, 1.0F);

            if (!(client.currentScreen instanceof UpdateEntryScreen))
                client.openScreen(new UpdateEntryScreen(entry));
        });
    }
}
