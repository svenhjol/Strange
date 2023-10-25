package svenhjol.strange.feature.ambient_music_discs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charmony.client.ClientFeature;

import java.util.Collections;
import java.util.LinkedList;

public class AmbientMusicDiscsClient extends ClientFeature {
    public static ResourceLocation soundHolder = null;

    @Override
    public void runWhenEnabled() {
        var registry = mod().registry();

        var tracks = new LinkedList<>(AmbientMusicDiscs.TRACKS);
        Collections.reverse(tracks);

        tracks.forEach(track -> {
            var item = AmbientMusicDiscs.items.getOrDefault(track, null);
            if (item == null) return;
            registry.itemTab(item, CreativeModeTabs.TOOLS_AND_UTILITIES, Items.MUSIC_DISC_PIGSTEP);
        });
    }
}
