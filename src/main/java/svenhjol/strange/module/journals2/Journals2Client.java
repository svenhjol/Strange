package svenhjol.strange.module.journals2;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.api.network.JournalMessages;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals2.photo.TakePhotoHandler;

import java.util.LinkedList;
import java.util.List;

@ClientModule(module = Journals2.class)
public class Journals2Client extends CharmModule {
    public static final List<ItemLike> BOOKMARK_ICONS = new LinkedList<>();

    public static @Nullable Journal2Data journal;
    public static PageTracker tracker;
    public static TakePhotoHandler photo;

    private KeyMapping keyBinding;

    @Override
    public void register() {
        tracker = new PageTracker();
        photo = new TakePhotoHandler();
    }

    @Override
    public void runWhenEnabled() {
        ClientTickEvents.END_WORLD_TICK.register(this::handleWorldTick);
        ClientPlayNetworking.registerGlobalReceiver(JournalMessages.CLIENT_SYNC_JOURNAL, this::handleSyncJournal);
        ClientPlayNetworking.registerGlobalReceiver(JournalMessages.CLIENT_OPEN_PAGE, this::handleOpenPage);

        initKeybind();
    }

    private void handleSyncJournal(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) return;

        client.execute(() -> {
            journal = Journal2Data.load(tag);
            LogHelper.debug(getClass(), "Received journal. " +
                journal.getLearnedRunes().size() + " learned runes, " +
                journal.getLearnedBiomes().size() + " learned biomes, " +
                journal.getLearnedDimensions().size() + " learned dimensions, " +
                journal.getLearnedStructures().size() + " learned structures.");
        });
    }

    private void handleOpenPage(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var page = buffer.readEnum(PageTracker.Page.class);

        client.execute(() -> {
            client.setScreen(tracker.getScreen(page));
            LogHelper.debug(getClass(), "Received request to open journal page `" + page + "` from server.");
        });
    }

    private void handleWorldTick(ClientLevel level) {
        if (keyBinding == null || level == null) return;

        ClientHelper.getClient().ifPresent(client -> {
            while (keyBinding.consumeClick()) {
                client.setScreen(tracker.getScreen());
            }

            photo.tick(client);
        });
    }

    private void initKeybind() {
        String name = "key.charm.openJournal";
        String category = "key.categories.inventory";

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            name,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            category
        ));
    }

    public static void sendMakeMap(Bookmark bookmark) {
        NetworkHelper.sendPacketToServer(JournalMessages.SERVER_MAKE_MAP, data -> data.writeNbt(bookmark.save()));
    }
}
