package svenhjol.strange.magic.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.magic.helper.MagicHelper;
import svenhjol.strange.magic.item.SpellBookItem;
import svenhjol.strange.magic.item.StaffItem;
import svenhjol.strange.magic.message.ServerStaffAction;
import svenhjol.strange.magic.spells.Spell;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.MAGIC, hasSubscriptions = true)
public class Staves extends MesonModule
{
    public static List<Item> staves = new ArrayList<>();
    public static long lastStaffInfo;

    @Override
    public void init()
    {
        staves.add(new StaffItem(this, "wooden", ItemTier.WOOD, 0.5F).setTransferMultiplier(0.65F));
        staves.add(new StaffItem(this, "stone", ItemTier.STONE, 0.75F).setCapacity(2));
        staves.add(new StaffItem(this, "iron", ItemTier.IRON, 1.0F).setTransferMultiplier(0.85F));
        staves.add(new StaffItem(this, "golden", ItemTier.GOLD, 0.75F).setTransferMultiplier(0.6F).setCapacity(3));
        staves.add(new StaffItem(this, "diamond", ItemTier.DIAMOND, 0.75F).setTransferMultiplier(0.85F).setCapacity(2));
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event)
    {
        if (event.getPlayer() != null
            && (event.getItemStack().getItem() instanceof SpellBookItem
                || event.getItemStack().getItem() instanceof StaffItem)
        ) {
            PlayerEntity player = event.getPlayer();
            ItemStack staff = null;

            if (event.getItemStack().getItem() instanceof SpellBookItem) {
                staff = SpellBookItem.getStaffFromOtherHand(player, event.getItemStack());
            } else {
                staff = event.getItemStack();
            }
            Hand hand = event.getHand();
            if (staff == null || !(staff.getItem() instanceof StaffItem)) return;

            PacketHandler.sendToServer(new ServerStaffAction(ServerStaffAction.CAST, hand));
        }
    }

    @SubscribeEvent
    public void onHeld(PlayerTickEvent event)
    {
        if (event.player != null
            && event.player.world.isRemote
            && event.player.world.getGameTime() % 10 == 0
        ) {
            PlayerEntity player = event.player;
            Hand hand = null;

            if (player.getHeldItemOffhand().getItem() instanceof StaffItem) {
                hand = Hand.OFF_HAND;
            } else if (player.getHeldItemMainhand().getItem() instanceof StaffItem) {
                hand = Hand.MAIN_HAND;
            }
            if (hand == null) return;

            ItemStack staff = player.getHeldItem(hand);
            long time = player.world.getGameTime();

            if (player.isSneaking() && (lastStaffInfo == 0 || time - lastStaffInfo > 20)) {

                LinkedList<Spell> spells = StaffItem.getSpells(staff);
                if (spells.isEmpty()) return;

                StringJoiner strings = new StringJoiner(", ");

                for (Spell spell : spells) {
                    strings.add(MagicHelper.getSpellInfoText(spell).getFormattedText());
                }

                player.sendStatusMessage(new TranslationTextComponent("event.strange.staff.contains", strings), true);
                lastStaffInfo = time;
            }
        }
    }
}
