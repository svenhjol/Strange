package svenhjol.strange.base.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.ItemHelper;
import svenhjol.strange.totems.module.TotemOfReturning;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.CORE, alwaysEnabled = true, hasSubscriptions = true,
    description = "Internal debugging tests for Strange.")
public class Debug extends MesonModule {
    private static Integer locating = 0;

    @Config(name = "Enable Locate Interrupt", description = "Allows functions that look for structures in the world to quit after a number of lookups." +
        "This prevents server timeouts when locations are too far away to be found.")
    public static boolean enableLocateInterrupt = false;

    @Config(name = "Locate Interrupt maximum lookups", description = "If `Enable Locate Interrupt` is enabled, this is the maximum number of lookups before searching gives up." +
        "Set this higher to allow wider searches.")
    public static int locateInterruptMaxLookups = 1500;

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

    public static void startLocating(Structure<?> structure) {
        if (enableLocateInterrupt) {
            locating = 0;
            Strange.LOG.debug("Starting locate loop count");
        }
    }

    public static boolean shouldStopLocating(Structure<?> structure) {
        if (!enableLocateInterrupt)
            return false;

        if (locating + 1 > locateInterruptMaxLookups) {
            Strange.LOG.debug("Locate loop count hit " + locateInterruptMaxLookups);
            locating = 0;
            return true;
        }

        locating++;

        return false;
    }
}
