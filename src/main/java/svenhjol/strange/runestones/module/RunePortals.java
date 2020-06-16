package svenhjol.strange.runestones.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.enums.ColorVariant;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.runestones.block.RunePortalBlock;
import svenhjol.strange.runestones.block.RunicAmethystBlock;
import svenhjol.strange.runestones.client.renderer.RunePortalTileEntityRenderer;
import svenhjol.strange.runestones.tileentity.RunePortalTileEntity;
import svenhjol.strange.traveljournal.storage.TravelJournalSavedData;

import java.util.ArrayList;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true)
public class RunePortals extends MesonModule {
    public static final List<RunicAmethystBlock> portalRunestones = new ArrayList<>();
    public static RunePortalBlock portal;

    @ObjectHolder("strange:rune_portal")
    public static TileEntityType<RunePortalTileEntity> tile;

    @OnlyIn(Dist.CLIENT)
    public static long clientTravelTicks;

    @Override
    public void init() {
        portal = new RunePortalBlock(this);

        // register TE
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "rune_portal");
        tile = TileEntityType.Builder.create(RunePortalTileEntity::new, portal).build(null);
        RegistryHandler.registerTile(tile, res);

        for (int i = 0; i < 26; i++) {
            portalRunestones.add(new RunicAmethystBlock(this, i));
        }
    }

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:amethyst");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(tile, RunePortalTileEntityRenderer::new);
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
            || !useColorRune && held.getItem() instanceof DyeItem) {
            ColorVariant color;
            if (useColorRune) {
                color = Charm.quarkCompat.getRuneColor(held);
            } else {
                color = ColorVariant.byIndex(((DyeItem)held.getItem()).getDyeColor().getId());
            }

            final boolean didActivate = tryActivate(serverWorld, pos, serverPlayer, color);
        }
    }

    @SubscribeEvent
    public void onPortalBlockBroken(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote()) {
            Block broken = event.getWorld().getBlockState(event.getPos()).getBlock();
            if (broken instanceof RunicAmethystBlock)
                breakSurroundingPortals((ServerWorld) event.getWorld(), event.getPos());
        }
    }

    public boolean tryActivate(ServerWorld world, BlockPos pos, ServerPlayerEntity player, ColorVariant color) {
        if (color == null)
            return false;

        final BlockState state = world.getBlockState(pos);
        List<Integer> order = new ArrayList<>();

        if (state.getBlock() instanceof RunicAmethystBlock) {

            // this tests the portal structure and gets the rune order. It's sensitive to axis and blockstate facing.
            Axis axis;

            if (world.getBlockState(pos.east()).getBlock() instanceof RunicAmethystBlock
                && world.getBlockState(pos.west()).getBlock() instanceof RunicAmethystBlock) {
                axis = Axis.X;
            } else if (world.getBlockState(pos.north()).getBlock() instanceof RunicAmethystBlock
                && world.getBlockState(pos.south()).getBlock() instanceof RunicAmethystBlock) {
                axis = Axis.Z;
            } else {
                return false;
            }

            switch (axis) {
                case X:
                    final BlockState eastState = world.getBlockState(pos.east(2).up(1));
                    final BlockState westState = world.getBlockState(pos.west(2).up(1));

                    if (eastState.get(RunicAmethystBlock.FACING) == Direction.NORTH) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(1 - i), order)) return false;
                        }
                    } else if (westState.get(RunicAmethystBlock.FACING) == Direction.SOUTH) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(1 - i), order)) return false;
                        }
                    }

                    break;

                case Z:
                    final BlockState northState = world.getBlockState(pos.north(2).up(1));
                    final BlockState southState = world.getBlockState(pos.south(2).up(1));

                    if (northState.get(RunicAmethystBlock.FACING) == Direction.WEST) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(1 - i), order)) return false;
                        }
                    } else if (southState.get(RunicAmethystBlock.FACING) == Direction.EAST) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(1 - i), order)) return false;
                        }
                    }

                    break;

                default:
                    return false;
            }

            // process the rune order, pull out dim and blockpos
            if (order.size() == 12) {
                Strange.LOG.debug("String order: " + order.toString());
                final List<Character> charkeys = new ArrayList<>(RunestoneHelper.getRuneCharMap().keySet());
                final List<Character> charvalues = new ArrayList<>(RunestoneHelper.getRuneCharMap().values());
                StringBuilder keyBuilder = new StringBuilder();
                TravelJournalSavedData data = TravelJournalSavedData.get(world);

                for (int o : order) {
                    Character c = charkeys.get(o);
                    keyBuilder.append(c);
                }

                String key = keyBuilder.toString();
                if (key.length() != 12) {
                    RunestoneHelper.runeError(world, pos, false, player);
                    return false;
                }

                if (!data.positions.containsKey(key) || !data.dimensions.containsKey(key)) {
                    RunestoneHelper.runeError(world, pos, false, player);
                    return false;
                }

                final BlockPos dest = data.positions.get(key);
                final int dim = data.dimensions.get(key);
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

    private boolean addOrder(ServerWorld world, BlockPos pos, List<Integer> order) {
        final BlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof RunicAmethystBlock))
            return false;

        RunicAmethystBlock b = (RunicAmethystBlock)s.getBlock();
        order.add(b.getRuneValue());
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectPortalTravelled(BlockPos pos) {
        World world = ClientHelper.getClientWorld();
        PlayerEntity player = ClientHelper.getClientPlayer();
        long time = world.getGameTime();
        if (clientTravelTicks == 0 || time - clientTravelTicks > 20) {
            BlockPos current = pos.add(-3, 0, -3);
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    BlockPos p = current.add(j, 0, k);
                    for (int i = 0; i < 6; ++i) {
                        double d0 = world.rand.nextGaussian() * 0.1D;
                        double d1 = world.rand.nextGaussian() * 0.1D;
                        double d2 = world.rand.nextGaussian() * 0.1D;
                        double dx = (float) p.getX() + MathHelper.clamp(world.rand.nextFloat(), 0.25f, 0.75f);
                        double dy = (float) p.getY() + 0.8f;
                        double dz = (float) p.getZ() + MathHelper.clamp(world.rand.nextFloat(), 0.25f, 0.75f);
                        world.addParticle(ParticleTypes.CLOUD, dx, dy, dz, d0, d1, d2);
                    }
                }
            }
            world.playSound(player, pos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.75F, 1.0F);
            clientTravelTicks = time;
        }
    }
}
