package svenhjol.strange.module.journals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.journals.network.*;
import svenhjol.strange.module.journals.photo.TakePhotoHandler;

import java.util.Arrays;
import java.util.Optional;

@ClientModule(module = Journals.class)
public class JournalsClient extends CharmModule {
    public static ClientReceiveJournal CLIENT_RECEIVE_JOURNAL;
    public static ClientReceiveBookmarkIcons CLIENT_RECEIVE_BOOKMARK_ICONS;
    public static ClientReceivePage CLIENT_RECEIVE_PAGE;
    public static ClientReceiveHint CLIENT_RECEIVE_HINT;
    public static ClientSendMakeMap CLIENT_SEND_MAKE_MAP;
    public static ClientSendOpenJournal CLIENT_SEND_OPEN_JOURNAL;

    private static @Nullable JournalData journal;

    public static PageTracker tracker;
    public static TakePhotoHandler photo;

    private KeyMapping keyBinding;
    public static boolean showJournalHint = false;

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
        CLIENT_RECEIVE_HINT = new ClientReceiveHint();
        CLIENT_SEND_MAKE_MAP = new ClientSendMakeMap();
        CLIENT_SEND_OPEN_JOURNAL = new ClientSendOpenJournal();

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

        ClientHelper.getClient().ifPresent(mc -> {
            while (keyBinding.consumeClick()) {
                CLIENT_SEND_OPEN_JOURNAL.send();
                mc.setScreen(tracker.getScreen());
            }

            if (mc.player != null) {
                checkJournalHint(mc.player);
            }

            photo.tick(mc);
        });
    }

    private void initKeybind() {
        String name = "key.charm.open_journal";
        String category = "key.categories.inventory";

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            name,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            category
        ));
    }

    private void checkJournalHint(Player player) {
        if (showJournalHint) {
            getJournal().ifPresent(journal -> {
                if (!journal.hasOpened()) {
                    var key = keyBinding.saveString();
                    if (key.contains(".")) {
                        var split = key.split("\\.");
                        if (split.length > 0) {
                            key = Arrays.asList(split).get(split.length - 1);
                        }
                    }

                    var message = new TranslatableComponent("gui.strange.journal.hint_open", key);
                    player.displayClientMessage(message, false);
                }
            });
            showJournalHint = false;
        }
    }
}
