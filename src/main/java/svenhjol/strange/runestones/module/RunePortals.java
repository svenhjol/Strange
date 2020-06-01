package svenhjol.strange.runestones.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
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
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.enums.ColorVariant;
import svenhjol.meson.handler.RegistryHandler;
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
import java.util.Map;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true)
public class RunePortals extends MesonModule {
    public static final List<RunicAmethystBlock> portalRunestones = new ArrayList<>();
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
                final Map<Character, Character> charmap = RunestoneHelper.getRuneCharMap();
                final List<Character> charkeys = new ArrayList<>(charmap.keySet());
                StringBuilder keyBuilder = new StringBuilder();

                for (int o : order) {
                    Character c = charkeys.get(o);
                    keyBuilder.append(c);
                }

                String key = keyBuilder.toString();
                if (key.length() != 12) {
                    runeError(world, pos, player);
                    return false;
                }

                TravelJournalSavedData data = TravelJournalSavedData.get(world);
                if (!data.positions.containsKey(key) || !data.dimensions.containsKey(key)) {
                    runeError(world, pos, player);
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

    private void runeError(World world, BlockPos pos, PlayerEntity player) {
        player.addPotionEffect(new EffectInstance(Effects.NAUSEA, 5 * 20));
        world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 2.5D, pos.getZ() + 0.5D, 0.75F, Explosion.Mode.NONE);
    }

    private boolean addOrder(ServerWorld world, BlockPos pos, List<Integer> order) {
        final BlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof RunicAmethystBlock))
            return false;

        RunicAmethystBlock b = (RunicAmethystBlock)s.getBlock();
        order.add(b.getRuneValue());
        return true;
    }
}
