package svenhjol.strange.totems.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeHelper;
import svenhjol.strange.totems.item.TotemOfExtractingItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfExtracting extends MesonModule
{
    public static TotemOfExtractingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 120;

    @Config(name = "Block extraction damage", description = "Damage taken when extracting normal blocks")
    public static int blockDamage = 4;

    @Config(name = "Tile extraction damage", description = "Damage taken when extracting tile entities (like chests)")
    public static int tileDamage = 16;

    @Config(name = "Heavy extraction damage", description = "Damage taken when extracing 'heavy' blocks (configurable blocks)")
    public static int heavyDamage = 32;

    @Config(name = "Heavy extraction blocks", description = "Extracting these blocks causes heavy damage to the totem.")
    public static List<String> heavy = new ArrayList<>(Arrays.asList(
        "minecraft:dragon_egg"
    ));

    @Config(name = "Single use blocks", description = "Extracting these blocks instantly destroys the totem.")
    public static List<String> singleUse = new ArrayList<>(Arrays.asList(
        "minecraft:obsidian",
        "minecraft:end_portal_frame",
        "minecraft:end_portal",
        "minecraft:spawner"
    ));

    @Config(name = "Immovable blocks", description = "Blocks that cannot be extracted with the totem.")
    public static List<String> blacklist = new ArrayList<>(Arrays.asList(
        "minecraft:bedrock"
    ));

    @Override
    public void init()
    {
        item = new TotemOfExtractingItem(this);
    }

    @SubscribeEvent
    public void onRightClick(RightClickBlock event)
    {
        PlayerEntity player = event.getPlayer();
        if (player == null) return;

        World world = event.getWorld();
        int dim = WorldHelper.getDimensionId(world);
        ItemStack held = player.getHeldItem(event.getHand());

        BlockPos srcPos = TotemOfExtractingItem.getPos(held);
        BlockPos destPos = event.getPos();

        if (player.isSneaking()) {

            // check blacklist
            BlockState state = world.getBlockState(destPos);
            String name = Objects.requireNonNull(state.getBlock().getRegistryName()).toString();

            if (blacklist.contains(name)) {
                if (world.isRemote) {
                    effectUnbinding(destPos);
                    player.playSound(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0F, 1.0F);
                }
            } else {
                TotemOfExtractingItem.setPos(held, destPos);
                TotemOfExtractingItem.setDim(held, dim);
                if (world.isRemote) {
                    effectBinding(destPos);
                    player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 0.8F);
                }
            }

        } else {

            if (srcPos == null) return; // not bound, just return
            boolean destroyed = false;

            BlockState srcState = world.getBlockState(srcPos);
            String srcName = Objects.requireNonNull(srcState.getBlock().getRegistryName()).toString();


            // check fail conditions
            if (dim != TotemOfExtractingItem.getDim(held)
                || blacklist.contains(srcName)
                || event.getFace() == null
            ) {
                player.playSound(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0F, 1.0F);
                return;
            }

            destPos = destPos.offset(event.getFace(), 1);

            // check for types, exceptions
            if (srcState.getProperties().contains(ChestBlock.TYPE)) {
                srcState = srcState.with(ChestBlock.TYPE, ChestType.SINGLE);
            }
            if (srcState.getProperties().contains(HorizontalBlock.HORIZONTAL_FACING)) {
                srcState = srcState.with(HorizontalBlock.HORIZONTAL_FACING, player.getHorizontalFacing().getOpposite());
            }
            if (srcState.getProperties().contains(BlockStateProperties.HALF)) {
                destroyed = true;
            }

            world.setBlockState(destPos, srcState, 2); // set the state at the new position


            TileEntity destTile = null;
            Block srcBlock = srcState.getBlock();

            // handle source block having tile entity (chest, etc)
            if (srcState.hasTileEntity()) {

                TileEntity srcTile = world.getTileEntity(srcPos); // get the TE from the source block
                world.removeTileEntity(srcPos); // remove TE from original position

                if (srcTile != null) {
                    CompoundNBT tag = srcTile.serializeNBT();
                    destTile = TileEntity.create(tag);

                    if (destTile != null) {
                        destTile.setPos(destPos);
                        destTile.validate();
                    }
                }
            }

            BlockState destState = world.getBlockState(destPos);
            world.removeBlock(srcPos, false);

            if (destTile != null) {
//                destTile = world.getTileEntity(destPos);

                if (!srcBlock.isValidPosition(destState, world, destPos)) {
                    world.setBlockState(destPos, destState, 2);
                    world.setTileEntity(destPos, destTile);
                    Block.spawnDrops(destState, world, destPos, destTile);
                    world.removeBlock(destPos, true);
                    destroyed = true;
                }

                if (!destroyed) {
                    world.setBlockState(destPos, destState);
                    world.setTileEntity(destPos, destTile);
                }
            }

            if (!destroyed) {
                world.setBlockState(destPos, srcState, 2);
                if (world.getTileEntity(destPos) != null) {
                    world.setBlockState(destPos, srcState, 0); // ?
                }

                if (destTile != null && !world.isRemote) {
                    world.setTileEntity(destPos, destTile);
                    destTile.updateContainingBlockInfo();
                }
                world.notifyNeighborsOfStateChange(destPos, srcBlock);
            }

            if (world.isRemote) {
                effectTransfer(srcPos, destPos);
            }

            // do totem damage/destruction
            int damageAmount;

            if (player.isCreative()) {
                damageAmount = 0;
            } else if (singleUse.contains(srcName)) {
                damageAmount = held.getMaxDamage();
            } else if (destTile != null) {
                damageAmount = tileDamage;
            } else {
                damageAmount = blockDamage;
            }

            int damage = StrangeHelper.damageTotem(player, held, damageAmount);
            if (damage > held.getMaxDamage()) {
                StrangeHelper.destroyTotem(player, held);
            } else {
                TotemOfExtractingItem.clearTags(held);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectTransfer(BlockPos src, BlockPos dest)
    {
        effectUnbinding(src);
        effectBinding(dest);
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectBinding(BlockPos src)
    {
        double spread = 0.7D;
        for (int i = 0; i < 8; i++) {
            double px = src.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = src.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = src.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0D, 0.08D, 0.0D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectUnbinding(BlockPos src)
    {
        double spread = 0.7D;
        for (int i = 0; i < 8; i++) {
            double px = src.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = src.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = src.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0.0D, 0.08D, 0.0D);
        }
    }
}
