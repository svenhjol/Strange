package svenhjol.strange.totems.module;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.scrolls.module.Scrollkeepers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class VillagersTradeTotems extends MesonModule
{
    public static final String LIBRARIAN = "librarian";
    public static List<Item> availableTotems = new ArrayList<>();

    @Config(name = "Trade level", description = "Level at which a villager will trade totems.")
    public static int tradeLevel = 1;

    @Config(name = "Base buy cost", description = "Minimum number of emeralds required to buy a totem.")
    public static int baseBuy = 8;

    @Config(name = "Base sell cost", description = "Minimum number of emeralds required to sell a totem.")
    public static int baseSell = 32;

    @Config(name = "Additional cost", description = "Maximum additional emeralds required to buy or sell.")
    public static int additional = 16;

    @Config(name = "Outer villagers only", description = "If true, only villagers from 'outer lands' villages have totem trades.\n" +
        "This has no effect if the Outerlands module is disabled.")
    public static boolean outerOnly = true;

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        if (Strange.loader.hasModule(TotemOfReturning.class)) availableTotems.add(TotemOfReturning.item);
        if (Strange.loader.hasModule(TotemOfShielding.class)) availableTotems.add(TotemOfShielding.item);
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event)
    {
        String useProfession = Strange.loader.hasModule(Scrollkeepers.class) ? Scrollkeepers.SCROLLKEEPER : LIBRARIAN;
        Int2ObjectMap<List<ITrade>> trades = event.getTrades();
        VillagerProfession profession = event.getType();

        if (profession.getRegistryName() == null || !profession.getRegistryName().getPath().equals(useProfession)) return;

        trades.get(tradeLevel).add(new TotemForEmeralds());
        trades.get(tradeLevel).add(new EmeraldsForTotem());
    }

    private static boolean isValidPosition(Entity merchant)
    {
        if (!outerOnly || !Strange.loader.hasModule(Outerlands.class)) return true;
        return merchant.getPosition().getX() > Outerlands.threshold || merchant.getPosition().getZ() > Outerlands.threshold;
    }

    static class TotemForEmeralds implements ITrade
    {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity merchant, Random rand)
        {
            if (!isValidPosition(merchant)) return null;
            ItemStack in1 = new ItemStack(Items.EMERALD, baseSell + (rand.nextInt(additional)));
            ItemStack out = new ItemStack(availableTotems.get(rand.nextInt(availableTotems.size())));
            return new MerchantOffer(in1, out, 3, 8, 0.2F);
        }
    }

    static class EmeraldsForTotem implements ITrade
    {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity merchant, Random rand)
        {
            if (!isValidPosition(merchant)) return null;
            ItemStack in1 = new ItemStack(availableTotems.get(rand.nextInt(availableTotems.size())));
            ItemStack out = new ItemStack(Items.EMERALD, baseBuy + (rand.nextInt(additional)));
            return new MerchantOffer(in1, out, 3, 8, 0.2F);
        }
    }
}
