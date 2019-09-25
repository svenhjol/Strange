package svenhjol.strange.totems.module;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
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
    public static int blockDamage = 6;

    @Config(name = "Tile extraction damage", description = "Damage taken when extracting tile entities (like chests)")
    public static int tileDamage = 16;

    @Config(name = "Heavy extraction damage", description = "Damage taken when extracing 'heavy' blocks (configurable blocks)")
    public static int heavyDamage = 30;

    @Config(name = "Heavy extraction blocks", description = "Extracting these blocks causes heavy damage to the totem.")
    public static List<String> heavy = new ArrayList<>(Arrays.asList(
        "minecraft:dragon_egg"
    ));

    @Config(name = "Single use blocks", description = "Extracting these blocks instantly destroys the totem.")
    public static List<String> singleUse = new ArrayList<>(Arrays.asList(
        "minecraft:obsidian",
        "minecraft:end_portal_frame",
        "minecraft:end_portal"
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
        BlockPos pos = event.getPos();

        ItemStack held = player.getHeldItem(event.getHand());
        BlockPos boundPos = TotemOfExtractingItem.getPos(held);

        int dim = WorldHelper.getDimensionId(world);

        if (player.isSneaking()) {

            // check blacklist
            BlockState state = world.getBlockState(pos);
            String name = Objects.requireNonNull(state.getBlock().getRegistryName()).toString();

            if (blacklist.contains(name)) {
                if (world.isRemote) {
                    effectUnbinding(pos);
                    player.playSound(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0F, 1.0F);
                }
            } else {
                TotemOfExtractingItem.setPos(held, pos);
                TotemOfExtractingItem.setDim(held, dim);
                if (world.isRemote) {
                    effectBinding(pos);
                    player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 0.8F);
                }
            }

        } else {

            if (boundPos == null) return;

            // if the totem is bound, move the block to the new position
            BlockState boundState = world.getBlockState(boundPos);
            String boundName = Objects.requireNonNull(boundState.getBlock().getRegistryName()).toString();
            BlockPos posUp = pos.up();

            // check fail conditions
            if (dim != TotemOfExtractingItem.getDim(held)
                || blacklist.contains(boundName)
            ) {
                player.playSound(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0F, 1.0F);
                return;
            }

            TileEntity boundTile = world.getTileEntity(boundPos);
            world.setBlockState(posUp, boundState, 2);

            if (boundTile != null) {
                CompoundNBT tag = boundTile.serializeNBT();
                TileEntity tile = TileEntity.create(tag);
                if (tile != null) {
                    tile.setPos(posUp);
                    tile.validate();

                    world.setTileEntity(pos, tile);
                    tile.updateContainingBlockInfo();

//                    world.notifyBlockUpdate(pos, boundState, boundState, 2);
                }
            }

            world.removeTileEntity(boundPos);
            world.removeBlock(boundPos, false);

            if (world.isRemote) {
                effectTransfer(boundPos, posUp);
            }

            // do totem damage/destruction
            int damageAmount;
            if (boundTile != null) {
                damageAmount = tileDamage;
            } else if (singleUse.contains(boundName)) {
                damageAmount = held.getMaxDamage();
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
            double px = src.getX() + 0.25D + (Math.random() - 0.5D) * spread;
            double py = src.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = src.getZ() + 0.25D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0D, 0.08D, 0.0D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectUnbinding(BlockPos src)
    {
        double spread = 0.7D;
        for (int i = 0; i < 8; i++) {
            double px = src.getX() + 0.25D + (Math.random() - 0.5D) * spread;
            double py = src.getY() + 0.25D + (Math.random() - 0.5D) * spread;
            double pz = src.getZ() + 0.25D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0.0D, 0.08D, 0.0D);
        }
    }
}
