package svenhjol.strange.module.runestones.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.helper.NbtHelper;
import svenhjol.strange.module.runes.Tier;
import svenhjol.strange.module.runestones.Runestones;

import java.util.HashMap;
import java.util.stream.Collectors;

@Id("strange:runestone_items")
public class ClientReceiveRunestoneItems extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            Runestones.ITEMS.clear();
            int count = 0;

            for (String d : tag.getAllKeys()) {
                var dimension = new ResourceLocation(d);
                var tag1 = tag.getCompound(d);

                for (String t : tag1.getAllKeys()) {
                    var tier = Tier.byName(t);
                    var items = NbtHelper.unpackStrings(tag1.getCompound(t)).stream()
                        .map(ResourceLocation::new)
                        .map(Registry.ITEM::get)
                        .collect(Collectors.toList());

                    count += items.size();
                    Runestones.ITEMS.computeIfAbsent(dimension, m -> new HashMap<>()).put(tier, items);
                }
            }
            LogHelper.debug(getClass(), "Received " + count + " runestone items.");
        });
    }
}
