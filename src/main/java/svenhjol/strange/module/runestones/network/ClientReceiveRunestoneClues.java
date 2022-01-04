package svenhjol.strange.module.runestones.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.runestones.Runestones;

import java.util.ArrayList;

@Id("strange:runestone_clues")
public class ClientReceiveRunestoneClues extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            Runestones.CLUES.clear();

            for (String key : tag.getAllKeys()) {
                var id = new ResourceLocation(key);
                var list = tag.getList(key, 8);

                Runestones.CLUES.computeIfAbsent(id, a -> new ArrayList<>());

                for (Tag i : list) {
                    Runestones.CLUES.get(id).add(i.getAsString());
                }
            }
        });
    }
}
