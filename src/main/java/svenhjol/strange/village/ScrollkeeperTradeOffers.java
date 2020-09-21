package svenhjol.strange.village;

import net.minecraft.entity.Entity;
import net.minecraft.village.TradeOffer;
import svenhjol.meson.helper.VillagerHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class ScrollkeeperTradeOffers {
    public static class ScrollForEmeralds extends VillagerHelper.SingleItemTypeTrade {
        private final int tier;

        public ScrollForEmeralds(int tier) {
            this.tier = tier;
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            experience = 0;

            return super.create(entity, random);
        }
    }
}
