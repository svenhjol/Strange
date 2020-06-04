package svenhjol.strange.totems.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.helper.StringHelper;
import svenhjol.strange.base.helper.LocationHelper;
import svenhjol.strange.base.helper.TotemHelper;

import javax.annotation.Nullable;
import java.util.List;

public class TotemOfReturningItem extends MesonItem {
    private static final String POS = "pos";
    private static final String DIM = "dim";
    private static final String GENERATED = "generated";

    public TotemOfReturningItem(MesonModule module) {
        super(module, "totem_of_returning", new Item.Properties()
            .group(ItemGroup.TRANSPORTATION)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
        );
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return getPos(stack) != null;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack held = player.getHeldItem(hand);
        BlockPos pos = getPos(held); // the position to teleport to
        int dim = getDim(held); // the dimension to teleport to

        // bind this totem to current location and exit
        if (PlayerHelper.isCrouching(player)) {
            BlockPos playerPos = player.getPosition();
            int playerDim = player.dimension.getId();
            setPos(held, playerPos);
            setDim(held, playerDim);

            player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 0.8F);
            return new ActionResult<>(ActionResultType.SUCCESS, held);
        } else if (pos != null && !world.isRemote) {
            teleport(world, player, pos, dim, held);
            boolean destroyed = TotemHelper.destroy(player, held);
            return new ActionResult<>(ActionResultType.SUCCESS, destroyed ? ItemStack.EMPTY : held);
        } else {
            player.sendStatusMessage(new TranslationTextComponent("totem.strange.returning.unbound"), true);
        }

        return super.onItemRightClick(world, player, hand);
    }

    public static void teleport(World world, PlayerEntity player, BlockPos pos, int dim, ItemStack stack) {
        if (!world.isRemote) {
            LocationHelper.addForcedChunk((ServerWorld)world, pos);
            if (getGenerated(stack)) {
                PlayerHelper.teleportSurface(player, pos, dim, TotemOfReturningItem::onTeleport);
            } else {
                PlayerHelper.teleport(player, pos, dim, TotemOfReturningItem::onTeleport);
            }
            LocationHelper.removeForcedChunk((ServerWorld)world, pos);
        }
    }

    private static void onTeleport(PlayerEntity player) {
        BlockPos pos = player.getPosition();
        for (int i = 0; i < 3; i++) {
            BlockPos pp = player.getPosition().add(0, i, 0);
            BlockState state = player.world.getBlockState(pp);
            if (state.isSolid()) {
                player.world.setBlockState(pp, Blocks.AIR.getDefaultState(), 2);
            }
        }
        player.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 40, 0));
        player.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Nullable
    public static BlockPos getPos(ItemStack stack) {
        long pos = ItemNBTHelper.getLong(stack, POS, 0);
        return pos == 0 ? null : BlockPos.fromLong(pos);
    }

    public static int getDim(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, DIM, 0);
    }

    public static boolean getGenerated(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, GENERATED, false);
    }

    public static void setPos(ItemStack stack, BlockPos pos) {
        ItemNBTHelper.setLong(stack, POS, pos.toLong());
    }

    public static void setDim(ItemStack stack, int dim) {
        ItemNBTHelper.setInt(stack, DIM, dim);
    }

    public static void setGenerated(ItemStack stack, boolean generated) {
        ItemNBTHelper.setBoolean(stack, GENERATED, generated);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> strings, ITooltipFlag flag) {
        BlockPos pos = getPos(stack);
        if (pos != null) {
            String strDim = String.valueOf(getDim(stack));
            String strPos = StringHelper.formatBlockPos(pos);
            strings.add(new StringTextComponent(I18n.format("totem.strange.returning.bound", strPos, strDim)));
        }
        super.addInformation(stack, world, strings, flag);
    }
}
