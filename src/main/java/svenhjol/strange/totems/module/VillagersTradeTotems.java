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
import svenhjol.meson.Meson;
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
public class VillagersTradeTotems extends MesonModule {
    public static final String LIBRARIAN = "librarian";
    public static final List<Item> availableTotems = new ArrayList<>();

    @Config(name = "Trade level", description = "Level at which a merchant will trade totems.")
    public static int tradeLevel = 1;

    @Config(name = "Base buy cost", description = "Minimum number of emeralds required to buy a totem.")
    public static int baseBuy = 8;

    @Config(name = "Additional cost", description = "Maximum additional emeralds required to buy.")
    public static int additional = 16;

    @Config(name = "Outer villagers only", description = "If true, only merchants from 'outer lands' villages have totem trades.\n" +
        "This has no effect if the Outerlands module is disabled.")
    public static boolean outerOnly = true;

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_totems");
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        availableTotems.add(Items.TOTEM_OF_UNDYING);
        if (Meson.isModuleEnabled("strange:totem_of_returning")) availableTotems.add(TotemOfReturning.item);
        if (Meson.isModuleEnabled("strange:totem_of_shielding")) availableTotems.add(TotemOfShielding.item);
        if (Meson.isModuleEnabled("strange:totem_of_preserving")) availableTotems.add(TotemOfPreserving.item);
        if (Meson.isModuleEnabled("strange:totem_of_flying")) availableTotems.add(TotemOfFlying.item);
        if (Meson.isModuleEnabled("strange:totem_of_enchanting")) availableTotems.add(TotemOfEnchanting.item);
        if (Meson.isModuleEnabled("strange:totem_of_transferring")) availableTotems.add(TotemOfTransferring.item);
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        String useProfession = Meson.isModuleEnabled("strange:spells") ? Scrollkeepers.SCROLLKEEPER : LIBRARIAN;
        Int2ObjectMap<List<ITrade>> trades = event.getTrades();
        VillagerProfession profession = event.getType();

        if (profession.getRegistryName() == null || !profession.getRegistryName().getPath().equals(useProfession))
            return;

        trades.get(tradeLevel).add(new EmeraldsForTotem());
    }

    private static boolean isValidPosition(Entity merchant) {
        if (!outerOnly || !Meson.isModuleEnabled("strange:outerlands")) return true;
        return merchant.getPosition().getX() > Outerlands.threshold || merchant.getPosition().getZ() > Outerlands.threshold;
    }

    public static class EmeraldsForTotem implements ITrade {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity merchant, Random rand) {
            if (!isValidPosition(merchant)) return null;
            ItemStack in1 = new ItemStack(availableTotems.get(rand.nextInt(availableTotems.size())));
            ItemStack out = new ItemStack(Items.EMERALD, baseBuy + (rand.nextInt(additional)));
            return new MerchantOffer(in1, out, 3, 8, 0.2F);
        }
    }
}
