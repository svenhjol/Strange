package svenhjol.strange.base;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import svenhjol.charm.tools.item.BoundCompassItem;
import svenhjol.charm.tools.module.CompassBinding;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.Objects;

public class QuestHelper
{
    public static void effectCompleted(PlayerEntity player, @Nullable ITextComponent message)
    {
        PlayerEntity p = player.world.isRemote ? player : null;
        player.world.playSound(null, player.getPosition(), StrangeSounds.QUEST_ACTION_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (message != null) {
            player.sendStatusMessage(message, true);
        }
    }

    public static void effectCounted(PlayerEntity player)
    {
        PlayerEntity p = player.world.isRemote ? player : null;
        player.world.playSound(null, player.getPosition(), StrangeSounds.QUEST_ACTION_COUNT, SoundCategory.PLAYERS, 1.0F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 1.1F);
    }

    public static void giveLocationItemToPlayer(PlayerEntity player, IQuest quest, BlockPos location, int dim)
    {
        // TODO should check if compass binding is enabled, if not, use a map
        ItemStack compass = new ItemStack(CompassBinding.item);
        compass.setDisplayName(new TranslationTextComponent(quest.getTitle()));
        BoundCompassItem.setPos(compass, location);
        BoundCompassItem.setDim(compass, dim);
        Objects.requireNonNull(compass.getTag()).putString(Quests.QUEST_ID, quest.getId());
        PlayerHelper.addOrDropStack(player, compass);
//        player.addItemStackToInventory(compass);
    }

    public static void removeQuestItemsFromPlayer(PlayerEntity player, IQuest quest)
    {
        // remove the quest helper equipment like compasses, maps, etc.
        ImmutableList<NonNullList<ItemStack>> inventories = PlayerHelper.getInventories(player);
        inventories.forEach(inv -> inv.forEach(stack -> {
            if (!stack.isEmpty() && stack.hasTag() && stack.getTag() != null) {
                String questTagId = stack.getTag().getString(Quests.QUEST_ID);
                if (questTagId.contains(quest.getId())) {
                    stack.shrink(1);
                }
            }
        }));
    }
}
