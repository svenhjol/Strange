package svenhjol.strange.feature.travel_journals.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.charmony.event.ClientTickEvent;
import svenhjol.charm.charmony.event.HudRenderEvent;
import svenhjol.charm.charmony.event.KeyPressEvent;
import svenhjol.charm.charmony.feature.RegisterHolder;
import svenhjol.strange.feature.travel_journals.TravelJournalsClient;
import svenhjol.strange.feature.travel_journals.common.Networking;

import java.util.function.Supplier;

public final class Registers extends RegisterHolder<TravelJournalsClient> {
    public final Supplier<String> openTravelJournalKey;
    public final Supplier<String> makeBookmarkKey;

    public Registers(TravelJournalsClient feature) {
        super(feature);
        var registry = feature.registry();

        openTravelJournalKey = registry.key("open_travel_journal",
            () -> new KeyMapping("key.strange.open_travel_journal", GLFW.GLFW_KEY_J, "key.categories.gameplay"));

        makeBookmarkKey = registry.key("make_bookmark",
            () -> new KeyMapping("key.strange.make_bookmark", GLFW.GLFW_KEY_B, "key.categories.gameplay"));

        // Client packet receivers
        registry.packetReceiver(Networking.S2CTakePhoto.TYPE, () -> feature.handlers::takePhotoReceived);
        registry.packetReceiver(Networking.S2CPhoto.TYPE, () -> feature.handlers::photoReceived);
    }

    @Override
    public void onEnabled() {
        KeyPressEvent.INSTANCE.handle(feature().handlers::keyPress);
        ClientTickEvent.INSTANCE.handle(feature().handlers::clientTick);
        HudRenderEvent.INSTANCE.handle(feature().handlers::hudRender);
    }
}
