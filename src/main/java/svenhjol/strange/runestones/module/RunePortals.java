package svenhjol.strange.runestones.module;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ObjectHolder;
import svenhjol.charm.Charm;
import svenhjol.charm.building.block.EnderPearlBlock;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.enums.ColorVariant;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.runestones.block.PortalRunestoneBlock;
import svenhjol.strange.runestones.block.RunePortalBlock;
import svenhjol.strange.runestones.client.renderer.RunePortalTileEntityRenderer;
import svenhjol.strange.runestones.tileentity.RunePortalTileEntity;

import java.util.ArrayList;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true)
public class RunePortals extends MesonModule {
    public static final List<PortalRunestoneBlock> portalRunestones = new ArrayList<>();

    public static RunePortalBlock portal;

    @ObjectHolder("strange:rune_portal")
    public static TileEntityType<RunePortalTileEntity> tile;

    @Override
    public void init() {
        portal = new RunePortalBlock(this);

        // register TE
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "rune_portal");
        tile = TileEntityType.Builder.create(RunePortalTileEntity::new, portal).build(null);
        RegistryHandler.registerTile(tile, res);

        for (int i = 0; i < 16; i++) {
            portalRunestones.add(new PortalRunestoneBlock(this, i));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(RunePortalTileEntity.class, new RunePortalTileEntityRenderer());
    }

    @SubscribeEvent
    public void onPortalActivated(RightClickBlock event) {
        boolean useColorRune = Charm.quarkCompat != null && Charm.quarkCompat.hasColorRuneModule();

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        PlayerEntity player = event.getPlayer();
        Hand hand = event.getHand();

        if (world.isRemote) return;
        ServerWorld serverWorld = (ServerWorld) world;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        ItemStack held = player.getHeldItem(hand);

        if (useColorRune && Charm.quarkCompat.isRune(held)
            || !useColorRune && held.getItem() == Items.DIAMOND) {
            final boolean didActivate = tryActivate(serverWorld, pos, serverPlayer, Charm.quarkCompat.getRuneColor(held));
        }
    }

    @SubscribeEvent
    public void onActivatorBlockBroken(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote()
            && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof EnderPearlBlock
            && Meson.isModuleEnabled(new ResourceLocation("charm:block_of_ender_pearls"))) {
            breakSurroundingPortals((ServerWorld)event.getWorld(), event.getPos());
        }
    }

    public boolean tryActivate(ServerWorld world, BlockPos pos, ServerPlayerEntity player, ColorVariant color) {
        if (!Meson.isModuleEnabled(new ResourceLocation("charm:block_of_ender_pearls")))
            return false;

        if (color == null)
            return false;

        final BlockState state = world.getBlockState(pos);
        List<Integer> order = new ArrayList<>();

        if (state.getBlock() instanceof EnderPearlBlock) {

            // this tests the portal structure and gets the rune order. It's sensitive to axis and "start" rune.
            Axis axis;

            if (world.getBlockState(pos.east()).getBlock() instanceof PortalRunestoneBlock
                && world.getBlockState(pos.west()).getBlock() instanceof PortalRunestoneBlock) {
                axis = Axis.X;
            } else if (world.getBlockState(pos.north()).getBlock() instanceof PortalRunestoneBlock
                && world.getBlockState(pos.south()).getBlock() instanceof PortalRunestoneBlock) {
                axis = Axis.Z;
            } else {
                return false;
            }

            switch (axis) {
                case X:
                    final PortalRunestoneBlock east = (PortalRunestoneBlock)world.getBlockState(pos.east()).getBlock();
                    final PortalRunestoneBlock west = (PortalRunestoneBlock)world.getBlockState(pos.west()).getBlock();

                    if (east.getRuneValue() == 0) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(2).up(3 - i), order)) return false;
                        }
                    } else if (west.getRuneValue() == 0) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(2).up(3 - i), order)) return false;
                        }
                    }

                    break;

                case Z:
                    final PortalRunestoneBlock north = (PortalRunestoneBlock)world.getBlockState(pos.north()).getBlock();
                    final PortalRunestoneBlock south = (PortalRunestoneBlock)world.getBlockState(pos.south()).getBlock();

                    if (north.getRuneValue() == 0) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(2).up(3 - i), order)) return false;
                        }
                    } else if (south.getRuneValue() == 0) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(2).up(3 - i), order)) return false;
                        }
                    }

                    break;

                default:
                    return false;
            }

            // process the rune order, pull out dim and blockpos
            if (order.size() == 18) {
                Strange.LOG.debug("String order: " + order.toString());
                StringBuilder sb = new StringBuilder();
                for (int o : order) {
                    sb.append(Integer.toHexString(o));
                }
                String s = sb.toString();
                if (s.length() < 5 || s.length() > 18) {
                    runeError(world, pos, player);
                    return false;
                }

                long l;
                int dim;

                try {
                    final String d0 = s.substring(0, 1);
                    final String d1 = s.substring(s.length() - 1);
                    final String dimHex = d0 + d1;
                    final String posHex = s.substring(1, s.length() - 1);

                    dim = Integer.parseUnsignedInt(dimHex, 16);
                    if (dim < 0 || dim > 255) {
                        runeError(world, pos, player);
                        return false;
                    }

                    dim -= 128;
                    Strange.LOG.debug("Dimension: " + dim);
                    Strange.LOG.debug("Trying to parse hex to long: " + posHex);
                    l = Long.parseUnsignedLong(posHex, 16);
                } catch (Exception e) {
                    Strange.LOG.debug("Failed: " + e.getMessage());
                    runeError(world, pos, player);
                    return false;
                }

                BlockPos dest = BlockPos.fromLong(l);
                Strange.LOG.debug("Destination: " + dest.toString());

                final int orientation = axis == Axis.X ? 0 : 1;
                final int colorId = color.ordinal();

                for (int a = -1; a < 2; a++) {
                    for (int b = 1; b < 4; b++) {
                        BlockPos p = axis == Axis.X ? pos.add(a, b, 0) : pos.add(0, b, a);
                        world.setBlockState(p, portal.getDefaultState().with(RunePortalBlock.AXIS, axis), 2);
                        portal.setPortal(world, p, dest, dim, colorId, orientation);
                    }
                }

                world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }

        return false;
    }

    public static void breakSurroundingPortals(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos blockpos = pos.offset(direction);
            if (world.isBlockLoaded(blockpos)) {
                BlockState s = world.getBlockState(blockpos);
                if (s.getBlock() instanceof RunePortalBlock)
                    ((RunePortalBlock)s.getBlock()).remove(world, blockpos);
            }
        }
    }

    public static BlockState getRunestoneBlock(int runeValue) {
        return portalRunestones.get(runeValue).getDefaultState();
    }

    private void runeError(World world, BlockPos pos, PlayerEntity player) {
        player.addPotionEffect(new EffectInstance(Effects.NAUSEA, 5 * 20));
        world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 1.5F, Explosion.Mode.NONE);
    }

    private boolean addOrder(ServerWorld world, BlockPos pos, List<Integer> order) {
        final BlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof PortalRunestoneBlock))
            return false;

        PortalRunestoneBlock b = (PortalRunestoneBlock)s.getBlock();
        final DyeColor color = PortalRunestoneBlock.getRuneColor(s);

        if (color == null)
            return false;

        order.add(b.getRuneValue());
        order.add(color.ordinal());
        return true;
    }
}
