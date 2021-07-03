package svenhjol.strange.module.travel_journals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.module.CharmModule;

public class TravelJournalItem extends CharmItem {
    public TravelJournalItem(CharmModule module) {
        super(module, "travel_journal", new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack journal = user.getItemInHand(hand);

        if (world.isClientSide) {
            ClientPlayNetworking.send(TravelJournals.MSG_SERVER_OPEN_JOURNAL, new FriendlyByteBuf(Unpooled.buffer()));
            return InteractionResultHolder.success(journal);
        }

        return super.use(world, user, hand);
    }
}
