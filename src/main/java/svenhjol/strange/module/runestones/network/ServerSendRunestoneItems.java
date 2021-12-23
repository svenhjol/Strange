package svenhjol.strange.module.runestones.network;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.helper.NbtHelper;
import svenhjol.strange.module.runes.Tier;
import svenhjol.strange.module.runestones.Runestones;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Id("strange:runestone_items")
public class ServerSendRunestoneItems extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var tag = new CompoundTag();

        for (Map.Entry<ResourceLocation, Map<Tier, List<Item>>> entry : Runestones.ITEMS.entrySet()) {
            var dim = entry.getKey();
            var packed = new CompoundTag();

            for (Map.Entry<Tier, List<Item>> tierEntry : entry.getValue().entrySet()) {
                var tier = tierEntry.getKey();
                var items = tierEntry.getValue();

                var itemList = NbtHelper.packStrings(items.stream()
                    .map(Registry.ITEM::getKey)
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toList()));

                packed.put(tier.getSerializedName(), itemList);
            }

            tag.put(dim.toString(), packed);
        }

        super.send(player, buf -> buf.writeNbt(tag));
    }
}
