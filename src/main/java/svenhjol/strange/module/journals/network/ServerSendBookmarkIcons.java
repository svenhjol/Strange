package svenhjol.strange.module.journals.network;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.helper.NbtHelper;
import svenhjol.strange.module.journals.Journals;

import java.util.stream.Collectors;

@Id("strange:bookmark_icons")
public class ServerSendBookmarkIcons extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var tag = NbtHelper.packStrings(Journals.BOOKMARK_ICONS.stream()
            .map(Registry.ITEM::getKey)
            .map(ResourceLocation::toString)
            .collect(Collectors.toList()));

        super.send(player, buf -> buf.writeNbt(tag));
    }
}
