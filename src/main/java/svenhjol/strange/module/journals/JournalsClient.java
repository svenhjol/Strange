package svenhjol.strange.module.journals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.module.journals.Journals.Page;
import svenhjol.strange.module.journals.data.JournalLocation;
import svenhjol.strange.module.journals.screen.JournalHomeScreen;
import svenhjol.strange.module.journals.screen.JournalKnowledgeScreen;
import svenhjol.strange.module.journals.screen.JournalLocationScreen;
import svenhjol.strange.module.journals.screen.JournalLocationsScreen;

import java.util.Optional;
import java.util.function.Consumer;

@ClientModule(module = Journals.class)
public class JournalsClient extends CharmModule {
    private static final int MAX_PHOTO_TICKS = 30;

    private KeyMapping keyBinding;
    private static JournalsData playerData;

    public static JournalLocation locationBeingPhotographed;
    public static int photoTicks = 0;

    @Override
    public void runWhenEnabled() {
        if (Journals.enableKeybind) {
            keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charm.openJournal",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "key.categories.inventory"
            ));

            ClientTickEvents.END_WORLD_TICK.register(this::handleWorldTick);
        }

        ClientPlayNetworking.registerGlobalReceiver(Journals.MSG_CLIENT_OPEN_JOURNAL, this::handleOpenJournal);
        ClientPlayNetworking.registerGlobalReceiver(Journals.MSG_CLIENT_SYNC_JOURNAL, this::handleSyncJournal);
        ClientPlayNetworking.registerGlobalReceiver(Journals.MSG_CLIENT_OPEN_LOCATION, this::handleOpenLocation);
    }

    /**
     * Always use this method to reference the current player's journal data on the client.
     * If you need to synchronise it, call sendSyncJournal() or sendOpenJournal() to sync and open
     */
    public static Optional<JournalsData> getPlayerData() {
        return Optional.ofNullable(playerData);
    }

    private void handleKeyPressed() {
        sendOpenJournal(Page.HOME);
    }

    private void handleWorldTick(Level level) {
        if (keyBinding == null || level == null)
            return;

        while (keyBinding.consumeClick()) {
            handleKeyPressed();
        }

        ClientHelper.getPlayer().ifPresent(player -> {
            if (photoTicks > 0) {

                if (locationBeingPhotographed == null) {
                    // reset photo timer
                    photoTicks = 0;
                } else if (++photoTicks > MAX_PHOTO_TICKS) {
                    Minecraft client = Minecraft.getInstance();
                    Screenshot.grab(
                        client.gameDirectory,
                        locationBeingPhotographed.getId() + ".png",
                        client.getMainRenderTarget(),
                        component -> {
                            if (client.player != null)
                                client.player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);

                            client.options.hideGui = false;
                            client.execute(() -> {
                                client.setScreen(new JournalLocationScreen(locationBeingPhotographed));
                                locationBeingPhotographed = null;
                            });
                            LogHelper.debug(this.getClass(), "Screenshot taken");
                        }
                    );
                    photoTicks = 0;
                }
            }
        });
    }

    private void handleSyncJournal(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        updatePlayerData(buffer.readNbt());
    }

    private void handleOpenJournal(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        updatePlayerData(buffer.readNbt());
        Page page = buffer.readEnum(Page.class);

        processPacketFromServer(client, mc -> {
            switch (page) {
                case LOCATIONS -> mc.setScreen(new JournalLocationsScreen());
                case KNOWLEDGE -> mc.setScreen(new JournalKnowledgeScreen());
                default -> mc.setScreen(new JournalHomeScreen());
            }
        });
    }

    private void handleOpenLocation(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag locationNbt = buffer.readNbt();
        if (locationNbt == null)
            return;

        JournalLocation newLocation = JournalLocation.fromNbt(locationNbt);
        processPacketFromServer(client, mc -> mc.setScreen(new JournalLocationScreen(newLocation)));
    }

    public static void sendSyncJournal() {
        NetworkHelper.sendEmptyPacketToServer(Journals.MSG_SERVER_SYNC_JOURNAL);
    }

    public static void sendOpenJournal(Page page) {
        NetworkHelper.sendPacketToServer(Journals.MSG_SERVER_OPEN_JOURNAL, data -> data.writeEnum(page));
    }

    public static void sendAddLocation() {
        NetworkHelper.sendEmptyPacketToServer(Journals.MSG_SERVER_ADD_LOCATION);
    }

    private void updatePlayerData(@Nullable CompoundTag nbt) {
        if (nbt != null)
            ClientHelper.getPlayer().ifPresent(player -> playerData = JournalsData.fromNbt(player, nbt));
    }

    private void processPacketFromServer(Minecraft client, Consumer<Minecraft> clientCallback) {
        client.execute(() -> {
            LocalPlayer player = client.player;
            if (player == null) return;
            clientCallback.accept(client);
        });
    }

}
