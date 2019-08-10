package svenhjol.strange.totems.feature;

import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.IForgeRegistry;
import svenhjol.meson.Feature;
import svenhjol.strange.totems.item.TotemOfReturningItem;

public class TotemOfReturning extends Feature
{
    public static ForgeConfigSpec.BooleanValue addToLoot;
    public static TotemOfReturningItem item;

    @Override
    public void configure()
    {
        super.configure();
    }

    @Override
    public void init()
    {
        super.init();
        item = new TotemOfReturningItem();
    }

    @Override
    public boolean hasSubscriptions()
    {
        return true;
    }

    @Override
    public void onRegisterItems(IForgeRegistry<Item> registry)
    {
        registry.register(item);
    }
}
