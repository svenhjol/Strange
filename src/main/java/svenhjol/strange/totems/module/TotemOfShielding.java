package svenhjol.strange.totems.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.TotemHelper;
import svenhjol.strange.base.loot.TreasureTotem;
import svenhjol.strange.totems.iface.ITreasureTotem;
import svenhjol.strange.totems.item.TotemOfShieldingItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfShielding extends MesonModule implements ITreasureTotem
{
    public static TotemOfShieldingItem item;

    @Config(name = "Durability", description = "Durability of the totem.")
    public static int durability = 128;

    @Config(name = "Damage multiplier", description = "Player damage is multiplied by this amount before being transferred to the totem." )
    public static double damageMultiplier = 0.5D;

    @Override
    public boolean shouldRunSetup()
    {
        return Meson.isModuleEnabled("strange:treasure_totems");
    }

    @Override
    public void init()
    {
        item = new TotemOfShieldingItem(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        TreasureTotem.availableTotems.add(this);
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity
            && !event.isCanceled()
        ) {
            PlayerEntity player = (PlayerEntity)event.getEntityLiving();

            for (Hand hand : Hand.values()) {
                ItemStack held = player.getHeldItem(hand);

                if (held.getItem() instanceof TotemOfShieldingItem) {
                    double damageAmount = event.getAmount() * damageMultiplier;
                    TotemHelper.damageOrDestroy(player, held, (int) Math.ceil(damageAmount));
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @Override
    public ItemStack getTreasureItem()
    {
        return new ItemStack(item);
    }
}
