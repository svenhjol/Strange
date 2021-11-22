package svenhjol.strange.module.scrollkeepers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import svenhjol.strange.module.scrolls.Scrolls;

import java.util.Random;

public class ScrollkeeperTradeOffers {
    public record ScrollForEmeralds(int tier) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {
            ItemStack in1 = new ItemStack(Items.EMERALD, tier);
            ItemStack out1 = new ItemStack(Scrolls.SCROLLS.get(tier));
            return new MerchantOffer(in1, out1, 10, 0, 0);
        }
    }
}
