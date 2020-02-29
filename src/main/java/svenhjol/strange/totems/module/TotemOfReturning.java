package svenhjol.strange.totems.module;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.loot.TreasureTotem;
import svenhjol.strange.totems.iface.ITreasureTotem;
import svenhjol.strange.totems.item.TotemOfReturningItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfReturning extends MesonModule implements ITreasureTotem
{
    public static TotemOfReturningItem item;

    @Override
    public boolean shouldBeEnabled()
    {
        return Meson.isModuleEnabled("strange:treasure_totems");
    }

    @Override
    public void init()
    {
        item = new TotemOfReturningItem(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        TreasureTotem.availableTotems.add(this);
    }

    @Override
    public ItemStack getTreasureItem()
    {
        return new ItemStack(item);
    }
}
