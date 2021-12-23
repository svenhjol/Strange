package svenhjol.strange.module.journals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.journals.network.ClientReceiveBookmarkIcons;
import svenhjol.strange.module.journals.network.ClientReceiveJournal;
import svenhjol.strange.module.journals.network.ClientReceivePage;
import svenhjol.strange.module.journals.network.ClientSendMakeMap;
import svenhjol.strange.module.journals.photo.TakePhotoHandler;

import java.util.Optional;

@ClientModule(module = Journals.class)
public class JournalsClient extends CharmModule {
    public static ClientReceiveJournal CLIENT_RECEIVE_JOURNAL;
    public static ClientReceiveBookmarkIcons CLIENT_RECEIVE_BOOKMARK_ICONS;
    public static ClientReceivePage CLIENT_RECEIVE_PAGE;
    public static ClientSendMakeMap CLIENT_SEND_MAKE_MAP;

    private static @Nullable JournalData journal;

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

        CLIENT_RECEIVE_JOURNAL = new ClientReceiveJournal();
        CLIENT_RECEIVE_BOOKMARK_ICONS = new ClientReceiveBookmarkIcons();
        CLIENT_RECEIVE_PAGE = new ClientReceivePage();
        CLIENT_SEND_MAKE_MAP = new ClientSendMakeMap();

        initKeybind();
    }

    public static Optional<JournalData> getJournal() {
        return Optional.ofNullable(journal);
    }

    public static void setJournal(JournalData journal) {
        JournalsClient.journal = journal;
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
}
