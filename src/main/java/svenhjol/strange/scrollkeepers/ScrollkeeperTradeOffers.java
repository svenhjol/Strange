package svenhjol.strange.scrollkeepers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import svenhjol.strange.scrolls.ScrollItem;
import svenhjol.strange.module.Scrolls;

import javax.annotation.Nullable;
import java.util.Random;

public class ScrollkeeperTradeOffers {
    public static class ScrollForEmeralds implements TradeOffers.Factory {
        private final int tier;

        public ScrollForEmeralds(int tier) {
            this.tier = tier;
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            ItemStack in1 = new ItemStack(Items.EMERALD, tier);
            ItemStack out1 = new ItemStack(Scrolls.SCROLL_TIERS.get(tier));
            ScrollItem.setScrollMerchant(out1, (MerchantEntity)entity);
            ScrollItem.setScrollRarity(out1, 0);
            return new TradeOffer(in1, out1, 10, 0, 0);
        }
    }
}
