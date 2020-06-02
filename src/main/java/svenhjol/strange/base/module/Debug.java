package svenhjol.strange.base.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.ItemHelper;
import svenhjol.strange.totems.module.TotemOfReturning;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.CORE, alwaysEnabled = true, hasSubscriptions = true,
    description = "Internal debugging tests for Strange.")
public class Debug extends MesonModule {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.player.world.isRemote
            && event.player.world.getGameTime() % 20 == 0
            && event.player.isCreative()
        ) {
            PlayerEntity player = event.player;
            if (player.getHeldItemOffhand().getItem() == Items.DIAMOND
                && player.getHeldItemMainhand().getItem() == TotemOfReturning.item
            ) {
                player.setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
                ItemStack totem = ItemHelper.getDistantTotem(player.world);
                if (totem != null)
                    player.setHeldItem(Hand.MAIN_HAND, totem);
            }
        }
    }
}
