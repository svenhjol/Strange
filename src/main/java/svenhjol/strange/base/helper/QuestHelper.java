package svenhjol.strange.base.helper;

import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import svenhjol.charm.tools.item.BoundCompassItem;
import svenhjol.charm.tools.module.CompassBinding;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.scrolls.module.Scrollkeepers;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class QuestHelper {
    public static void effectCompleted(PlayerEntity player, @Nullable ITextComponent message) {
        player.world.playSound(null, player.getPosition(), StrangeSounds.QUEST_ACTION_COMPLETE, SoundCategory.PLAYERS, 0.6F, 1.0F);

        if (message != null) {
            player.sendStatusMessage(message, true);
        }
    }

    public static void effectCounted(PlayerEntity player) {
        player.world.playSound(null, player.getPosition(), StrangeSounds.QUEST_ACTION_COUNT, SoundCategory.PLAYERS, 0.6F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 1.1F);
    }

    public static void giveLocationItemToPlayer(PlayerEntity player, IQuest quest, BlockPos location, int dim) {
        ItemStack stack;
        TranslationTextComponent title = new TranslationTextComponent(quest.getTitle());

        if (Meson.isModuleEnabled("charm:compass_binding")) {
            stack = new ItemStack(CompassBinding.item);
            BoundCompassItem.setPos(stack, location);
            BoundCompassItem.setDim(stack, dim);
        } else {
            World world = player.world;
            stack = FilledMapItem.setupNewMap(world, location.getX(), location.getZ(), (byte) 2, true, true);
            FilledMapItem.func_226642_a_((ServerWorld)world, stack);
            MapData.addTargetDecoration(stack, location, "+", MapDecoration.Type.TARGET_X);
        }

        stack.setDisplayName(title);
        PlayerHelper.addOrDropStack(player, stack);
    }

    public static void giveQuestItemToPlayer(PlayerEntity player, ItemStack stack) {
        PlayerHelper.addOrDropStack(player, stack);
    }

    @Nullable
    public static BlockPos getScrollkeeperNearPlayer(PlayerEntity player, IQuest quest, int range) {
        List<VillagerEntity> scrollkeepers = player.world.getEntitiesWithinAABB(VillagerEntity.class, player.getBoundingBox().grow(range))
            .stream()
            .filter(m -> m.getVillagerData().getProfession() == Scrollkeepers.profession)
            .collect(Collectors.toList());

        if (scrollkeepers.isEmpty()) return null;

        if (quest.getSeller() == Scrollkeepers.ANY_SELLER) {
            return scrollkeepers.get(0).getPosition();
        }

        for (VillagerEntity scrollkeeper : scrollkeepers) {
            if (scrollkeeper.getUniqueID().equals(quest.getSeller())) {
                return scrollkeeper.getPosition();
            }
        }

        return null;
    }

    public static boolean isBoundToScrollkeeper(IQuest quest) {
        return !quest.getSeller().equals(Scrollkeepers.ANY_SELLER);
    }
}
