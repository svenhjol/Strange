package svenhjol.strange.module.discoveries.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.discoveries.Discoveries;

@Id("strange:discoveries")
public class ServerSendDiscoveries extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var discoveries = Discoveries.getDiscoveries().orElse(null);
        if (discoveries == null) return;

        var tag = new CompoundTag();
        discoveries.branch.save(tag);

        send(player, buf -> buf.writeNbt(tag));
    }
}
