package svenhjol.strange.module.travel_journals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Screenshooter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
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
    public static KeyBinding keyBinding;

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
            keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.charm.openTravelJournal",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.inventory"
            ));

            ClientTickEvents.END_WORLD_TICK.register(world -> {
                if (keyBinding == null || world == null)
                    return;

                while (keyBinding.wasPressed()) {
                    triggerOpenTravelJournal();
                }
            });
        }

        ClientPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_CLIENT_RECEIVE_ENTRIES, this::handleClientReceiveEntries);
        ClientPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_CLIENT_RECEIVE_ENTRY, this::handleClientReceiveEntry);
    }

    public static boolean isPlayerAtEntryPosition(PlayerEntity player, TravelJournalEntry entry) {
        return entry.pos != null && PosHelper.getDistanceSquared(player.getBlockPos(), entry.pos) < TravelJournals.SCREENSHOT_DISTANCE;
    }

    public static void triggerOpenTravelJournal() {
        ClientPlayNetworking.send(TravelJournals.MSG_SERVER_OPEN_JOURNAL, new PacketByteBuf(Unpooled.buffer()));
    }

    private void handleGuiSetup(MinecraftClient client, int width, int height, List<Selectable> buttons) {
        if (client.player == null
            || !(client.currentScreen instanceof InventoryScreen)
            || client.player.isCreative())
            return;

        HandledScreen<?> screen = (HandledScreen<?>)client.currentScreen;

        int x = ScreenHelper.getX(screen) + 158;
        int y = ScreenHelper.getY(screen) + 5;

        TexturedButtonWidget button = new TexturedButtonWidget(x, y, 12, 12, 20, 0, 12, StrangeResources.INVENTORY_BUTTONS, click
            -> triggerOpenTravelJournal());

        screen.addDrawableChild(button);
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

                Screenshooter.saveScreenshot(
                    client.runDirectory,
                    entryHavingScreenshot.id + ".png",
                    win.getFramebufferWidth() / 8,
                    win.getFramebufferHeight() / 8,
                    client.getFramebuffer(),
                    i -> {
                        client.player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
                        client.options.hudHidden = false;
                        client.execute(() -> {
                            client.openScreen(new TravelJournalUpdateEntryScreen(entryHavingScreenshot));
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
        NbtCompound tag = data.readNbt();
        if (tag == null)
            return;

        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null)
                return;

            String uuid = player.getUuidAsString();
            NbtElement entriesTag = tag.get(uuid);
            if (entriesTag == null)
                return;

            entries = TravelJournalsHelper.getEntriesFromNbtList((NbtList)entriesTag);
            client.openScreen(new TravelJournalHomeScreen());
        });
    }

    private void handleClientReceiveEntry(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        NbtCompound tag = data.readNbt();
        if (tag == null)
            return;

        client.execute(() -> {
            TravelJournalEntry entry = new TravelJournalEntry(tag);

            if (client.player != null)
                client.player.playSound(SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN, 1.0F, 1.0F);

            if (!(client.currentScreen instanceof TravelJournalUpdateEntryScreen))
                client.openScreen(new TravelJournalUpdateEntryScreen(entry));
        });
    }
}
