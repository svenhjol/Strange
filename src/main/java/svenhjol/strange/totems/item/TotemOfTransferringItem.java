package svenhjol.strange.totems.item;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.base.helper.TotemHelper;
import svenhjol.strange.totems.module.TotemOfTransferring;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class TotemOfTransferringItem extends MesonItem
{
    public static String STATE = "state";
    public TotemOfTransferringItem(MesonModule module)
    {
        super(module, "totem_of_transferring", new Properties()
            .group(ItemGroup.TOOLS)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return hasState(stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack holdable = player.getHeldItem(hand);
        if (hasState(holdable)) {
            CompoundNBT savedState = getState(holdable);
            INBT nbtState = savedState.get("state");
            if (nbtState == null)
                return new ActionResult<>(ActionResultType.FAIL, holdable);

            BlockState srcState = NBTUtil.readBlockState((CompoundNBT) nbtState);
            AtomicBoolean destroyed = new AtomicBoolean(false);

            // check for exceptions, modify state accordingly
            if (srcState.getProperties().contains(ChestBlock.TYPE)) {
                srcState = srcState.with(ChestBlock.TYPE, ChestType.SINGLE);
            }
            if (srcState.getProperties().contains(HorizontalBlock.HORIZONTAL_FACING)) {
                srcState = srcState.with(HorizontalBlock.HORIZONTAL_FACING, player.getHorizontalFacing().getOpposite());
            }
            if (!isValidState(srcState)) {
                destroyed.set(true);
            }

            final BlockState finalSrcState = srcState;

            BlockRayTraceResult result = WorldHelper.getBlockLookedAt(player);
            BlockPos destPos = result.getPos().offset(result.getFace(), 1);
            world.setBlockState(destPos, finalSrcState, 2); // set the state at the new position

            TileEntity destTile = null;
            Block srcBlock = finalSrcState.getBlock();

            if (savedState.contains("tile")) {
                INBT tileNbt = savedState.get("tile");
                if (tileNbt != null) {
                    destTile = TileEntity.create((CompoundNBT)tileNbt);
                    if (destTile != null) {
                        destTile.setPos(destPos);
                        destTile.validate();
                    }
                }
            }

            BlockState destState = world.getBlockState(destPos);

            if (destTile != null) {
                if (!srcBlock.isValidPosition(destState, world, destPos)) {
                    world.setBlockState(destPos, destState, 2);
                    world.setTileEntity(destPos, destTile);
                    Block.spawnDrops(destState, world, destPos, destTile);
                    world.removeBlock(destPos, true);
                    destroyed.set(true);
                }

                if (!destroyed.get()) {
                    world.setBlockState(destPos, destState);
                    world.setTileEntity(destPos, destTile);
                }
            }

            if (!destroyed.get()) {
                world.setBlockState(destPos, finalSrcState, 2);
                if (world.getTileEntity(destPos) != null) {
                    world.setBlockState(destPos, finalSrcState, 0); // ?
                }

                if (destTile != null && !world.isRemote) {
                    world.setTileEntity(destPos, destTile);
                    destTile.updateContainingBlockInfo();
                }
                world.notifyNeighborsOfStateChange(destPos, srcBlock);
            }

            TotemHelper.destroy(player, holdable);

            return new ActionResult<>(ActionResultType.SUCCESS, holdable);

        } else {
            // get the block the player is looking at
            BlockRayTraceResult result = WorldHelper.getBlockLookedAt(player);
            BlockPos pos = result.getPos();
            BlockState state = world.getBlockState(result.getPos());
            String srcName = Objects.requireNonNull(state.getBlock().getRegistryName()).toString();

            // check blacklist
            if (!player.isCreative()) {
                if (!isValidState(state) || TotemOfTransferring.transferBlacklist.contains(srcName)) {
                    world.playSound(null, player.getPosition(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    player.getCooldownTracker().setCooldown(holdable.getItem(), 20);
                    return new ActionResult<>(ActionResultType.FAIL, holdable);
                }
            }

            CompoundNBT savedState = new CompoundNBT();

            // store state
            savedState.put("state", NBTUtil.writeBlockState(state));

            // store tile
            if (state.hasTileEntity()) {
                TileEntity tile = world.getTileEntity(pos); // get the TE from the source block
                if (tile != null) {
                    savedState.put("tile", tile.serializeNBT());
                }
                world.removeTileEntity(pos);
            }

            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);

            if (!world.isRemote) {
                double px = pos.getX() + 0.5D;
                double py = pos.getY() + 1.0D;
                double pz = pos.getZ() + 0.5D;
                ((ServerWorld) world).spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 20, 0.1D, 0.1D, 0.1D, 0.5D);
            }

            putState(holdable, savedState);
            return new ActionResult<>(ActionResultType.SUCCESS, holdable);
        }
    }

    public static CompoundNBT getState(ItemStack totem)
    {
        return totem.getOrCreateChildTag(STATE);
    }

    public static boolean hasState(ItemStack totem)
    {
        CompoundNBT state = getState(totem);
        boolean b = state.isEmpty();
        return !b;
    }

    public static void putState(ItemStack totem, CompoundNBT state)
    {
        ItemNBTHelper.setCompound(totem, STATE, state);
    }

    private boolean isValidState(BlockState state)
    {
        boolean invalid = state.getMaterial() == Material.AIR
            || state.has(BlockStateProperties.BED_PART)
            || state.has(BlockStateProperties.HALF)
            || state.has(BlockStateProperties.BOTTOM)
            || state.has(BlockStateProperties.DOUBLE_BLOCK_HALF);

        return !invalid;
    }
}
