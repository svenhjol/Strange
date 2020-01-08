package svenhjol.strange.spells.module;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true, configureEnabled = false)
public class VillagersTradeSpellBooks extends MesonModule
{
    public static final String LIBRARIAN = "librarian";

    @Config(name = "Base sell cost", description = "Minimum number of emeralds required to sell a spell bool.")
    public static int baseSell = 8;

    @Config(name = "Additional cost", description = "Maximum additional emeralds required to buy.")
    public static int additional = 8;

    @Config(name = "Max uses", description = "Maximum number of times the merchant will sell before locking.")
    public static int maxUses = 2;

    @Config(name = "Trade level", description = "Level at which a merchant will trade spell bools.")
    public static int tradeLevel = 3;

    @Config(name = "Allow rare spells", description = "Allows all spells rather than common ones.")
    public static boolean useRare = false;

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && Strange.hasModule(Spells.class);
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event)
    {
        Int2ObjectMap<List<ITrade>> trades = event.getTrades();
        VillagerProfession profession = event.getType();
        if (profession.getRegistryName() == null || !profession.getRegistryName().getPath().equals(LIBRARIAN)) return;

        trades.get(tradeLevel).add(new SpellBoolForEmeralds());
    }

    public static class SpellBoolForEmeralds implements ITrade
    {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity merchant, Random rand)
        {
            ItemStack book = new ItemStack(SpellBooks.book);

            ItemStack in1 = new ItemStack(Items.EMERALD, baseSell + rand.nextInt(additional));
            ItemStack out = SpellBooks.attachRandomSpell(book, rand, useRare);
            return new MerchantOffer(in1, out, maxUses, 8, 0.2F);
        }
    }
}
