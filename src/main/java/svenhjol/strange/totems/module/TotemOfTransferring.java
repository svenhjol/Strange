package svenhjol.strange.totems.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.item.TotemOfTransferringItem;

import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfTransferring extends MesonModule
{
    public static TotemOfTransferringItem item;

    public static List<String> transferBlacklist = Arrays.asList(
        "minecraft:bedrock",
        "minecraft:end_portal_frame",
        "minecraft:end_portal",
        "minecraft:iron_door",
        "charm:rune_portal_frame",
        "charm:rune_portal"
    );

    public static List<String> transferHeavy = Arrays.asList(
        "minecraft:spawner",
        "minecraft:dragon_egg"
    );

    @Override
    public void init()
    {
        item = new TotemOfTransferringItem(this);
    }
}
