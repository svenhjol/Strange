package svenhjol.strange.module.scrollkeepers;

import svenhjol.strange.module.scrolls.ScrollItem;
import svenhjol.strange.module.scrolls.Scrolls;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import java.util.Random;

public class ScrollkeeperTradeOffers {
    public static class ScrollForEmeralds implements VillagerTrades.ItemListing {
        private final int tier;

        public ScrollForEmeralds(int tier) {
            this.tier = tier;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {
            ItemStack in1 = new ItemStack(Items.EMERALD, tier);
            ItemStack out1 = new ItemStack(Scrolls.SCROLL_TIERS.get(tier));
            ScrollItem.setScrollMerchant(out1, (AbstractVillager)entity);
            ScrollItem.setScrollRarity(out1, 0);
            return new MerchantOffer(in1, out1, 10, 0, 0);
        }
    }
}
