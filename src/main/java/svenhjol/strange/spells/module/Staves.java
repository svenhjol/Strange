package svenhjol.strange.spells.module;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.spells.item.StaffItem;
import svenhjol.strange.spells.spells.Spell;

import java.util.ArrayList;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true, configureEnabled = false)
public class Staves extends MesonModule
{
    public static List<Item> staves = new ArrayList<>();

    @Override
    public boolean shouldBeEnabled()
    {
        return Meson.isModuleEnabled("strange:spells");
    }

    @Override
    public void init()
    {
        staves.add(new StaffItem(this, "wooden", ItemTier.WOOD));
    }

    public static void effectEnchantStaff(ServerPlayerEntity player, Spell spell, int particles, double xOffset, double yOffset, double zOffset, double speed)
    {
        Spells.effectEnchant((ServerWorld)player.world, player.getPositionVec(), spell, particles, xOffset, yOffset, zOffset, speed);
    }
}
