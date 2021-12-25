package svenhjol.strange.module.journals.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.NbtHelper;
import svenhjol.strange.module.journals.Journals;

import java.util.stream.Collectors;

@Id("strange:bookmark_icons")
public class ClientReceiveBookmarkIcons extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            var icons = NbtHelper.unpackStrings(tag).stream()
                .map(ResourceLocation::new)
                .map(Registry.ITEM::get)
                .collect(Collectors.toList());

            Journals.BOOKMARK_ICONS.clear();
            Journals.BOOKMARK_ICONS.addAll(icons);
            LogHelper.debug(Strange.MOD_ID, getClass(), "Received " + icons.size() + " bookmark icons.");
        });
    }
}
