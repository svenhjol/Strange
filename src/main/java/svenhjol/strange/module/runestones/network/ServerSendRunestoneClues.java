package svenhjol.strange.module.runestones.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.runestones.Runestones;

import java.util.List;
import java.util.Map;

@Id("strange:runestone_clues")
public class ServerSendRunestoneClues extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var tag = new CompoundTag();

        for (Map.Entry<ResourceLocation, List<String>> entry : Runestones.CLUES.entrySet()) {
            var id = entry.getKey();
            var clues = entry.getValue();
            var list = new ListTag();

            for (String clue : clues) {
                list.add(StringTag.valueOf(clue));
            }

            tag.put(id.toString(), list);
        }

        super.send(player, buf -> buf.writeNbt(tag));
    }
}
