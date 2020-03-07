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
import svenhjol.strange.totems.item.TotemOfTransferringItem;

import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfTransferring extends MesonModule implements ITreasureTotem {
    public static TotemOfTransferringItem item;

    public static List<String> transferBlacklist = Arrays.asList(
        "minecraft:bedrock",
        "minecraft:end_portal_frame",
        "minecraft:end_portal",
        "minecraft:iron_door",
        "charm:rune_portal_frame",
        "charm:rune_portal"
    );

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_totems");
    }

    @Override
    public void init() {
        item = new TotemOfTransferringItem(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        TreasureTotem.availableTotems.add(this);
    }

    @Override
    public ItemStack getTreasureItem() {
        return new ItemStack(item);
    }
}
