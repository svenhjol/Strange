package svenhjol.strange.spells.module;

import net.minecraft.item.DyeColor;
import svenhjol.charm.tools.item.MoonstoneItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;

import java.util.ArrayList;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true,
    description = "")
public class Moonstones extends MesonModule
{
    public static List<MoonstoneItem> items = new ArrayList<>();

    @Override
    public void init()
    {
        for (DyeColor value : DyeColor.values()) {
            items.add(new MoonstoneItem(this, value));
        }
    }

//    @SubscribeEvent
//    public void onMoonstoneUse(RightClickBlock event)
//    {
//        if (!event.getWorld().isRemote
//            && event.getPlayer() != null
//            && event.getPlayer().getHeldItem(event.getHand()).getItem() instanceof MoonstoneItem
//        ) {
//            PlayerEntity player = event.getPlayer();
//            Hand hand = event.getHand();
//            ItemStack held = player.getHeldItem(hand);
//            BlockPos pos = event.getPos();
//
//            if (player.isSneaking()) {
//                MoonstoneItem.setStonePos(held, pos);
//                MoonstoneItem.setStoneDim(held, player.dimension.getId());
//                player.swingArm(hand);
//            }
//        }
//    }
}
