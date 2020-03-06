package svenhjol.strange.enchantments.module;

import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.EnchantmentsHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.enchantment.GrowthEnchantment;
import svenhjol.strange.enchantments.iface.ITreasureEnchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class Growth extends MesonModule implements ITreasureEnchantment
{
    public static GrowthEnchantment enchantment;
    public static int[] range = new int[] {5, 3, 5};
    public static int damageOnActivate = 15;

    @Override
    public boolean shouldRunSetup()
    {
        return Meson.isModuleEnabled("strange:treasure_enchantments");
    }

    @Override
    public void init()
    {
        enchantment = new GrowthEnchantment(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        TreasureEnchantments.availableEnchantments.add(this);
    }

    @SubscribeEvent
    public void onRightClick(RightClickBlock event)
    {
        if (event.getEntityLiving() != null
            && !event.getEntityLiving().world.isRemote
            && EnchantmentsHelper.hasEnchantment(enchantment, event.getItemStack())
        ) {
            ServerWorld world = (ServerWorld)event.getEntityLiving().world;
            BlockPos pos = event.getPos();
            ItemStack stack = event.getItemStack();

            Stream<BlockPos> inRange = BlockPos.getAllInBox(pos.add(-range[0], -range[1], -range[2]), pos.add(range[0], range[1], range[2]));
            List<BlockPos> blocks = inRange.map(BlockPos::toImmutable).collect(Collectors.toList());

            for (BlockPos blockPos : blocks) {
                boolean didGrow = false;
                BlockState state = world.getBlockState(blockPos);
                Block block = state.getBlock();
                if (block instanceof IPlantable) {
                    if (block instanceof IGrowable) {
                        IGrowable growable = (IGrowable)state.getBlock();
                        if (growable.canGrow(world, blockPos, state, world.isRemote)) {
                            growable.grow(world, world.rand, blockPos, state);
                            didGrow = true;
                        }
                    }
                }
                if (block instanceof NetherWartBlock && state.has(NetherWartBlock.AGE)) {
                    Integer currentAge = state.get(NetherWartBlock.AGE);
                    if (currentAge < 3) {
                        world.setBlockState(blockPos, state.with(NetherWartBlock.AGE, ++currentAge), 2);
                        didGrow = true;
                    }
                }
                if (block instanceof SugarCaneBlock && world.isAirBlock(blockPos.up()) && state.has(SugarCaneBlock.AGE)) {
                    BlockState newState = state.with(SugarCaneBlock.AGE, 15);
                    SugarCaneBlock sugarCaneBlock = (SugarCaneBlock)block;
                    sugarCaneBlock.tick(newState, world, blockPos, world.rand);
                    didGrow = true;
                }
                if (block instanceof SaplingBlock) {
                    BlockState newState = state.with(SaplingBlock.STAGE, 1);
                    SaplingBlock saplingBlock = (SaplingBlock)block;
                    saplingBlock.tick(newState, world, blockPos, world.rand);
                    didGrow = true;
                }

                if (didGrow) {
                    world.playEvent(2005, blockPos, 0);

                    world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_FILL_SUCCESS, SoundCategory.BLOCKS, 1.0F, 0.9F);

                    if (stack.attemptDamageItem(damageOnActivate, world.rand, null))
                        stack.setCount(0);
                }
            }
        }
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments()
    {
        return new HashMap<Enchantment, Integer>() {{ put(enchantment, 1); }};
    }

    @Override
    public ItemStack getTreasureItem()
    {
        ItemStack treasure;
        Random rand = new Random();

        if (rand.nextFloat() < 0.5F) {
            treasure = new ItemStack(Items.DIAMOND_HOE);
        } else {
            treasure = new ItemStack(Items.DIAMOND_SHOVEL);
        }

        treasure.setDisplayName(new TranslationTextComponent("item.strange.treasure.growth", treasure.getDisplayName()));
        return treasure;
    }

    @Override
    public DyeColor getColor()
    {
        return DyeColor.GREEN;
    }
}
