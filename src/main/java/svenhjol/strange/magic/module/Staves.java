package svenhjol.strange.magic.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.magic.item.SpellBookItem;
import svenhjol.strange.magic.item.StaffItem;
import svenhjol.strange.magic.message.ServerStaffAction;

import java.util.ArrayList;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.MAGIC)
public class Staves extends MesonModule
{
    public static List<Item> staves = new ArrayList<>();

    @Override
    public void init()
    {
        staves.add(new StaffItem(this, "wooden", ItemTier.WOOD, 0.5F).setTransferMultiplier(0.65F));
        staves.add(new StaffItem(this, "stone", ItemTier.STONE, 0.75F).setCapacity(2));
        staves.add(new StaffItem(this, "iron", ItemTier.IRON, 1.0F).setTransferMultiplier(0.85F));
        staves.add(new StaffItem(this, "golden", ItemTier.GOLD, 0.75F).setTransferMultiplier(0.6F).setCapacity(3));
        staves.add(new StaffItem(this, "diamond", ItemTier.DIAMOND, 0.75F).setTransferMultiplier(0.85F).setCapacity(2));
    }

    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event)
    {
        if (event.getPlayer() != null
            && event.getItemStack().getItem() instanceof SpellBookItem
        ) {
            PlayerEntity player = event.getPlayer();
            ItemStack book = event.getItemStack();
            ItemStack staff = SpellBookItem.getStaffInOtherHand(player, book);
            Hand hand = event.getHand();
            if (staff == null || !(staff.getItem() instanceof StaffItem)) return;

            PacketHandler.sendToServer(new ServerStaffAction(ServerStaffAction.CAST, hand));
        }
    }
}
