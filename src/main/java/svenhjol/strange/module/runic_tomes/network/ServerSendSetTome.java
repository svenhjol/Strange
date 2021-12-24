package svenhjol.strange.module.runic_tomes.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

@Id("strange:set_lectern_tome")
public class ServerSendSetTome extends ServerSender {
    public void send(ServerPlayer player, ItemStack tome) {
        var tag = new CompoundTag();
        tome.save(tag);

        super.send(player, buf -> buf.writeNbt(tag));
    }
}
