package svenhjol.strange.module.runic_tomes.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomesClient;

@Id("strange:set_lectern_tome")
public class ClientReceiveSetTome extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        CompoundTag tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            RunicTomesClient.tomeHolder = ItemStack.of(tag);
            String runes = RunicTomeItem.getRunes(RunicTomesClient.tomeHolder);

            if (runes.isEmpty()) {
                LogHelper.error(getClass(), "Could not fetch runes from tome");
                return;
            }

            LogHelper.debug(getClass(), "Tome set from server. Runes = " + runes);
        });
    }
}
