package svenhjol.strange.enchantments.module;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.EnchantmentsHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.enchantment.IceAspectEnchantment;
import svenhjol.strange.enchantments.iface.ITreasureEnchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class IceAspect extends MesonModule implements ITreasureEnchantment
{
    public static IceAspectEnchantment enchantment;
    public static int damageOnActivate = 5;
    public static double duration = 5.0D;
    public static int amplifier = 2;
    public static int[] range = new int[] {3, 2, 3};

    @Override
    public boolean shouldRunSetup()
    {
        return Meson.isModuleEnabled("strange:treasure_enchantments");
    }

    @Override
    public void init()
    {
        enchantment = new IceAspectEnchantment(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        TreasureEnchantments.availableEnchantments.add(this);
    }

    @SubscribeEvent
    public void onAttack(LivingHurtEvent event)
    {
        if (!event.isCanceled()
            && event.getEntityLiving() != null
            && !event.getEntityLiving().world.isRemote
        ) {
            Entity trueSource = event.getSource().getTrueSource();
            LivingEntity target = event.getEntityLiving();

            if (trueSource instanceof LivingEntity && target != null) {
                LivingEntity attacker = (LivingEntity) trueSource;
                ItemStack held = attacker.getHeldItemMainhand();

                if (EnchantmentsHelper.hasEnchantment(enchantment, held)) {
                    int level = EnchantmentHelper.getEnchantmentLevel(enchantment, held);
                    ServerWorld world = (ServerWorld)target.world;
                    BlockPos pos = target.getPosition();
                    Stream<BlockPos> inRange = BlockPos.getAllInBox(pos.add(-range[0], -range[1], -range[2]), pos.add(range[0], range[1], range[2]));
                    List<BlockPos> blocks = inRange.map(BlockPos::toImmutable).collect(Collectors.toList());

                    for (BlockPos p : blocks) {
                        BlockState state = world.getBlockState(p);
                        if (state.isNormalCube(world, pos) && state.isSolid()
                            && state.getMaterial() != Material.SNOW
                            && world.isAirBlock(p.up())
                            && world.rand.nextFloat() < (level * 0.2F)
                        ) {
                            world.setBlockState(p.up(), Blocks.SNOW.getDefaultState(), 2);
                        } else if (state.getBlock() == Blocks.WATER
                            && world.rand.nextFloat() < (level * 0.2F)
                        ) {
                            world.setBlockState(p, Blocks.FROSTED_ICE.getDefaultState(), 2);
                        }
                    }

                    target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, level * (int) (duration * 20), level * amplifier));

                    world.playSound(null, pos, SoundEvents.BLOCK_GLASS_HIT, SoundCategory.BLOCKS, 1.0F, 0.9F);

                    if (held.attemptDamageItem(damageOnActivate, attacker.world.rand, null))
                        held.setCount(0);
                }
            }
        }
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments()
    {
        return new HashMap<Enchantment, Integer>() {{ put(enchantment, new Random().nextInt(enchantment.getMaxLevel()) + 1); }};
    }

    @Override
    public ItemStack getTreasureItem()
    {
        ItemStack treasure;
        Random rand = new Random();

        if (rand.nextFloat() < 0.5F) {
            treasure = new ItemStack(Items.DIAMOND_SWORD);
        } else {
            treasure = new ItemStack(Items.DIAMOND_AXE);
        }

        treasure.setDisplayName(new TranslationTextComponent("item.strange.treasure.ice_aspect", treasure.getDisplayName()));
        return treasure;
    }

    @Override
    public DyeColor getColor()
    {
        return DyeColor.LIGHT_BLUE;
    }
}
