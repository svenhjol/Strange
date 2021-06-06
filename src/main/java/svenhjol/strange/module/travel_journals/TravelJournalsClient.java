package svenhjol.strange.module.travel_journals;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.Charm;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.event.SetupGuiCallback;
import svenhjol.charm.helper.PosHelper;
import svenhjol.charm.helper.ScreenHelper;
import svenhjol.charm.module.CharmClientModule;
import svenhjol.charm.module.CharmModule;
import svenhjol.strange.init.StrangeResources;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.module.travel_journals.screen.TravelJournalHomeScreen;
import svenhjol.strange.module.travel_journals.screen.TravelJournalUpdateEntryScreen;

import java.util.ArrayList;
import java.util.List;

public class TravelJournalsClient extends CharmClientModule {
    public static List<TravelJournalEntry> entries = new ArrayList<>();
    public static KeyMapping keyBinding;

    public static TravelJournalEntry entryHavingScreenshot;
    public static int screenshotTicks;
    public static int lastEntryPage;

    public TravelJournalsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void init() {
        SetupGuiCallback.EVENT.register(this::handleGuiSetup);
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);

        if (TravelJournals.enableKeybind) {
            keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charm.openTravelJournal",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.inventory"
            ));

            ClientTickEvents.END_WORLD_TICK.register(world -> {
                if (keyBinding == null || world == null)
                    return;

                while (keyBinding.consumeClick()) {
                    triggerOpenTravelJournal();
                }
            });
        }

        ClientPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_CLIENT_RECEIVE_ENTRIES, this::handleClientReceiveEntries);
        ClientPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_CLIENT_RECEIVE_ENTRY, this::handleClientReceiveEntry);
    }

    public static boolean isPlayerAtEntryPosition(Player player, TravelJournalEntry entry) {
        return entry.pos != null && PosHelper.getDistanceSquared(player.blockPosition(), entry.pos) < TravelJournals.SCREENSHOT_DISTANCE;
    }

    public static void triggerOpenTravelJournal() {
        ClientPlayNetworking.send(TravelJournals.MSG_SERVER_OPEN_JOURNAL, new FriendlyByteBuf(Unpooled.buffer()));
    }

    private void handleGuiSetup(Minecraft client, int width, int height, List<NarratableEntry> buttons) {
        if (client.player == null
            || !(client.screen instanceof InventoryScreen)
            || client.player.isCreative())
            return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)client.screen;

        int x = ScreenHelper.getX(screen) + 158;
        int y = ScreenHelper.getY(screen) + 5;

        ImageButton button = new ImageButton(x, y, 12, 12, 20, 0, 12, StrangeResources.INVENTORY_BUTTONS, click
            -> triggerOpenTravelJournal());

        screen.addRenderableWidget(button);
    }

    private void handlePlayerTick(Player player) {
        if (!player.level.isClientSide)
            return;

        if (screenshotTicks > 0) {
            if (entryHavingScreenshot == null) {
                screenshotTicks = 0;
            } else if (++screenshotTicks > 30) {
                Minecraft client = Minecraft.getInstance();
                Window win = client.getWindow();

                Screenshot.grab(
                    client.gameDirectory,
                    entryHavingScreenshot.id + ".png",
                    win.getWidth() / 8,
                    win.getHeight() / 8,
                    client.getMainRenderTarget(),
                    i -> {
                        client.player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
                        client.options.hideGui = false;
                        client.execute(() -> {
                            client.setScreen(new TravelJournalUpdateEntryScreen(entryHavingScreenshot));
                            entryHavingScreenshot = null;
                        });
                        Charm.LOG.debug("Screenshot taken");
                    }
                );
                screenshotTicks = 0;
            }
        }
    }

    private void handleClientReceiveEntries(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag tag = data.readNbt();
        if (tag == null)
            return;

        client.execute(() -> {
            LocalPlayer player = client.player;
            if (player == null)
                return;

            String uuid = player.getStringUUID();
            Tag entriesTag = tag.get(uuid);
            if (entriesTag == null)
                return;

            entries = TravelJournalsHelper.getEntriesFromNbtList((ListTag)entriesTag);
            client.setScreen(new TravelJournalHomeScreen());
        });
    }

    private void handleClientReceiveEntry(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag tag = data.readNbt();
        if (tag == null)
            return;

        client.execute(() -> {
            TravelJournalEntry entry = new TravelJournalEntry(tag);

            if (client.player != null)
                client.player.playSound(SoundEvents.VILLAGER_WORK_LIBRARIAN, 1.0F, 1.0F);

            if (!(client.screen instanceof TravelJournalUpdateEntryScreen))
                client.setScreen(new TravelJournalUpdateEntryScreen(entry));
        });
    }
}
