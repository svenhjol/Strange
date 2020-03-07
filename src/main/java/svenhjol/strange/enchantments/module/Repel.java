package svenhjol.strange.enchantments.module;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.EnchantmentsHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.enchantment.RepelEnchantment;
import svenhjol.strange.enchantments.iface.ITreasureEnchantment;

import java.util.*;
import java.util.function.Predicate;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class Repel extends MesonModule implements ITreasureEnchantment {
    public static RepelEnchantment enchantment;
    public static int[] range = new int[]{5, 3, 5};
    public static int damageOnActivate = 15;

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_enchantments");
    }

    @Override
    public void init() {
        enchantment = new RepelEnchantment(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        TreasureEnchantments.availableEnchantments.add(this);
    }

    @SubscribeEvent
    public void onRightClick(RightClickBlock event) {
        if (event.getEntityLiving() != null
            && !event.getEntityLiving().world.isRemote
            && EnchantmentsHelper.hasEnchantment(enchantment, event.getItemStack())
        ) {
            ServerWorld world = (ServerWorld) event.getEntityLiving().world;
            LivingEntity entity = event.getEntityLiving();
            ItemStack stack = event.getItemStack();
            int level = EnchantmentHelper.getEnchantmentLevel(enchantment, stack);

            List<LivingEntity> entities;
            AxisAlignedBB area = entity.getBoundingBox().grow(range[0], range[1], range[2]);
            Predicate<LivingEntity> selector = Objects::nonNull;
            entities = world.getEntitiesWithinAABB(LivingEntity.class, area, selector);

            for (LivingEntity e : entities) {
                e.knockBack(entity, level * 2.0F, MathHelper.sin(entity.rotationYaw * ((float) Math.PI / 180F)), -MathHelper.cos(entity.rotationYaw * ((float) Math.PI / 180F)));
            }

            world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.4F, 0.88F);

            if (stack.attemptDamageItem(damageOnActivate, world.rand, null))
                stack.setCount(0);

            entity.swingArm(Hand.MAIN_HAND);
        }
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return new HashMap<Enchantment, Integer>() {{
            put(enchantment, 1);
        }};
    }

    @Override
    public ItemStack getTreasureItem() {
        ItemStack treasure;
        Random rand = new Random();

        if (rand.nextFloat() < 0.5F) {
            treasure = new ItemStack(Items.DIAMOND_SWORD);
        } else {
            treasure = new ItemStack(Items.DIAMOND_AXE);
        }

        treasure.setDisplayName(new TranslationTextComponent("item.strange.treasure.repel", treasure.getDisplayName()));
        return treasure;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.MAGENTA;
    }
}
