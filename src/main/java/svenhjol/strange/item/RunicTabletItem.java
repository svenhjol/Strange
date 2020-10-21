package svenhjol.strange.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PosHelper;

public class RunicTabletItem extends TabletItem {
    public RunicTabletItem(CharmModule module, String name) {
        super(module, name, new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!(stack.getItem() instanceof TabletItem))
            return TypedActionResult.fail(stack);

        BlockPos pos = TabletItem.getPos(stack);
        if (pos == null)
            return TypedActionResult.fail(stack);

        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 0.75F, 1.2F);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (world.isClient)
            return;

        BlockPos pos = TabletItem.getPos(stack);
        Identifier dimension = TabletItem.getDimension(stack);
        boolean exact = TabletItem.getExact(stack);

        if (!DimensionHelper.isDimension(world, dimension))
            return;

        if (pos == null) {
            Charm.LOG.warn("Encoded position was null");
            return;
        }

        if (!exact) {
            pos = PosHelper.getSurfacePos(world, pos);
            if (pos == null)
                return;
        }

        user.teleport(pos.getX(), pos.getY(), pos.getZ(), true);
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.5F, 1.25F);
        stack.damage(1, user, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 40000;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }
}
