package svenhjol.strange.feature.ambient_music_discs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charmony.annotation.ClientFeature;
import svenhjol.charmony.base.CharmFeature;
import svenhjol.strange.StrangeClient;

@ClientFeature(mod = StrangeClient.MOD_ID, canBeDisabled = false)
public class AmbientMusicDiscsClient extends CharmFeature {
    public static ResourceLocation soundHolder = null;

    @Override
    public void runWhenEnabled() {
        var registry = StrangeClient.instance().registry();

        AmbientMusicDiscs.TRACKS.forEach(track -> {
            var item = AmbientMusicDiscs.items.getOrDefault(track, null);
            if (item == null) return;
            registry.itemTab(item, CreativeModeTabs.TOOLS_AND_UTILITIES, Items.MUSIC_DISC_PIGSTEP);
        });
    }
}
