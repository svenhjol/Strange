package svenhjol.strange.spells.module;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ObjectHolder;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.block.SpellLecternBlock;
import svenhjol.strange.spells.client.SpellLecternsClient;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.item.MoonstoneItem;
import svenhjol.strange.spells.item.SpellBookItem;
import svenhjol.strange.spells.spells.Spell;
import svenhjol.strange.spells.tile.SpellLecternTileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true)
public class SpellLecterns extends MesonModule
{
    public static SpellLecternBlock block;

    @ObjectHolder("strange:spell_lectern")
    public static TileEntityType<SpellLecternTileEntity> tile;

    @OnlyIn(Dist.CLIENT)
    public static SpellLecternsClient client;

    @Override
    public void init()
    {
        // register block and tile
        block = new SpellLecternBlock();
        block.register(this, "spell_lectern");
        tile = TileEntityType.Builder.create(SpellLecternTileEntity::new, block).build(null);
        RegistryHandler.registerTile(tile, new ResourceLocation(Strange.MOD_ID, "spell_lectern"));
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        client = new SpellLecternsClient();
        client.setupClient(event);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END
            && event.player != null
            && !event.player.world.isRemote
            && event.player.world.getGameTime() % 12 == 0
        ) {
            PlayerEntity player = event.player;
            World world = player.world;

            // check for looking at a spellbook lectern
            int len = 6;
            Vec3d v1 = player.getEyePosition(1.0F);
            Vec3d v2 = player.getLook(1.0F);
            Vec3d v3 = v1.add(v2.x * len, v2.y * len, v2.z * len);
            BlockRayTraceResult result = world.rayTraceBlocks(new RayTraceContext(v1, v3, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
            BlockPos p = result.getPos();
            BlockState lookedAt = world.getBlockState(p);
            if (lookedAt.getBlock() == block) {
                TileEntity tile1 = world.getTileEntity(p);
                if (tile1 instanceof SpellLecternTileEntity) {
                    SpellLecternTileEntity lectern1 = (SpellLecternTileEntity)tile1;
                    ItemStack book1 = lectern1.getBook();
                    if (book1.getItem() instanceof SpellBookItem) {
                        Spell spell1 = SpellBookItem.getSpell(book1);
                        ITextComponent message = SpellsHelper.getSpellInfoText(spell1);
                        player.sendStatusMessage(message, true);
                    }
                }
            }

            // check for moonstone charging
            Hand hand = null;
            if (player.getHeldItemMainhand().getItem() instanceof MoonstoneItem) {
                hand = Hand.MAIN_HAND;
            } else if (player.getHeldItemOffhand().getItem() instanceof MoonstoneItem) {
                hand = Hand.OFF_HAND;
            }
            if (hand == null) return;
            if (player.world.rand.nextFloat() > 0.5F) return;

            ItemStack stone = player.getHeldItem(hand);
            if (MoonstoneItem.hasSpell(stone)) return; // don't try and add spell to Moonstone with spell
            int[] range = new int[] { 1, 2, 1 };

            BlockPos pos = player.getPosition();
            Stream<BlockPos> inRange = BlockPos.getAllInBox(pos.add(-range[0], -range[1], -range[2]), pos.add(range[0], range[1], range[2]));
            List<BlockPos> blocks = inRange.map(BlockPos::toImmutable).collect(Collectors.toList());
            List<BlockPos> validPositions = new ArrayList<>();

            for (BlockPos blockPos : blocks) {
                if (world.getBlockState(blockPos).getBlock() == block) {
                    validPositions.add(blockPos);
                }
            }

            if (validPositions.isEmpty()) return;
            double dist = 64;
            BlockPos closestPos = null;

            for (BlockPos validPos : validPositions) {
                double between = WorldHelper.getDistanceSq(pos, validPos);
                if (between < dist) {
                    closestPos = validPos;
                    dist = between;
                }
            }
            if (closestPos == null) return;
            Vec3d vec = new Vec3d(closestPos.getX() + 0.5D, closestPos.getY() + 0.5D, closestPos.getZ() + 0.5D);

            TileEntity tile = world.getTileEntity(closestPos);
            if (!(tile instanceof SpellLecternTileEntity)) return;

            SpellLecternTileEntity lectern = (SpellLecternTileEntity)tile;
            ItemStack book = lectern.getBook();
            if (book.getItem() != SpellBooks.book) return;

            Spell spell = SpellBookItem.getSpell(book);
            if (spell == null) return;

            if (!player.isCreative() && player.experienceLevel < spell.getApplyCost()) {
                player.sendStatusMessage(SpellsHelper.getSpellInfoText(spell, "event.strange.spellbook.not_enough_xp"), true);
                return;
            }

            player.addExperienceLevel(-spell.getApplyCost());
            MoonstoneItem.putSpell(stone, spell);

            world.playSound(null, player.getPosition(), StrangeSounds.SPELL_BOOK_CHARGE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            Moonstones.effectEnchantStone((ServerPlayerEntity)player, spell, 15, 0.1D, 0.4D, 0.1D, 3.0D);
            Spells.effectEnchant((ServerWorld)world, vec, spell, 15, 0D, 0.5D, 0D, 4.0D);

            if (book.attemptDamageItem(8, world.rand, (ServerPlayerEntity) player)) {
                SpellLecternBlock.restoreLectern(world, closestPos, world.getBlockState(closestPos));
            }
        }
    }
}
