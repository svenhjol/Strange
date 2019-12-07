package svenhjol.strange.spells.spells;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.spells.item.StaffItem;
import svenhjol.strange.spells.module.Spells;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ExtractionSpell extends Spell
{
    public ExtractionSpell()
    {
        super("extraction");
        this.element = Element.EARTH;
        this.affect = Affect.FOCUS;
        this.duration = 1.0F;
        this.castCost = 15;
    }

    @Override
    public boolean activate(PlayerEntity player, ItemStack staff)
    {
        // get the block the player is looking at
        BlockRayTraceResult result = WorldHelper.getBlockLookedAt(player);

        World world = player.world;
        BlockPos pos = result.getPos();
        BlockState state = world.getBlockState(result.getPos());

        String srcName = Objects.requireNonNull(state.getBlock().getRegistryName()).toString();

        // check blacklist
        if (!player.isCreative()) {
            boolean invalid = state.has(BlockStateProperties.DOUBLE_BLOCK_HALF)
                || state.has(BlockStateProperties.BED_PART)
                || state.has(BlockStateProperties.HALF)
                || state.has(BlockStateProperties.BOTTOM)
                || world.isAirBlock(pos);

            if (invalid || Spells.extractionBlacklist.contains(srcName)) {
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                player.getCooldownTracker().setCooldown(staff.getItem(), 20);
                return false;
            }

            if (Spells.extractionHeavy.contains(srcName)) {
                int playerLevel = player.experienceLevel;
                int levelCost = 10;

                if (playerLevel < levelCost) {
                    player.sendStatusMessage(new TranslationTextComponent("event.strange.spellbook.not_enough_xp"), true);
                    player.getCooldownTracker().setCooldown(staff.getItem(), 20);
                    return false;
                }
                player.addExperienceLevel(-levelCost);
            }
        }

        CompoundNBT meta = new CompoundNBT();

        // store state
        meta.put("state", NBTUtil.writeBlockState(state));

        // store tile
        if (state.hasTileEntity()) {
            TileEntity tile = world.getTileEntity(pos); // get the TE from the source block
            if (tile != null) {
                meta.put("tile", tile.serializeNBT());
            }
            world.removeTileEntity(pos);
        }

        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);

        if (!world.isRemote) {
            double px = pos.getX() + 0.5D;
            double py = pos.getY() + 1.0D;
            double pz = pos.getZ() + 0.5D;
            ((ServerWorld)world).spawnParticle(Spells.enchantParticles.get(this.getElement()), px, py, pz, 20, 0.1D, 0.1D, 0.1D, 0.5D);
        }

        StaffItem.putMeta(staff, meta);

        return true;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        World world = player.world;
        CompoundNBT meta = StaffItem.getMeta(staff);

        if (!meta.contains("state")) return;
        INBT nbtState = meta.get("state");
        if (nbtState == null) return;

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
        castFocus(player, result -> {

            BlockPos destPos = result.getPos().offset(result.getFace(), 1);
            world.setBlockState(destPos, finalSrcState, 2); // set the state at the new position

            TileEntity destTile = null;
            Block srcBlock = finalSrcState.getBlock();

            if (meta.contains("tile")) {
                INBT tileNbt = meta.get("tile");
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
        });

        didCast.accept(true);
    }

    private boolean isValidState(BlockState state)
    {
        boolean invalid = state.getProperties().contains(BlockStateProperties.HALF)
            || state.getProperties().contains(BlockStateProperties.BED_PART);

        return !invalid;
    }
}
