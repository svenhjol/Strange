package svenhjol.strange.traveljournals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

public class TravelJournalItem extends CharmItem {
    public TravelJournalItem(CharmModule module) {
        super(module, "travel_journal", new Item.Settings()
            .group(ItemGroup.MISC)
            .maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack journal = user.getStackInHand(hand);

        if (world.isClient) {
            ClientPlayNetworking.send(TravelJournals.MSG_SERVER_OPEN_JOURNAL, new PacketByteBuf(Unpooled.buffer()));
            return TypedActionResult.success(journal);
        }

        return super.use(world, user, hand);
    }
}
