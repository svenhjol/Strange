package svenhjol.strange.enchantments.module;

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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.EnchantmentsHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.enchantment.ShulkingEnchantment;
import svenhjol.strange.enchantments.iface.ITreasureEnchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class Shulking extends MesonModule implements ITreasureEnchantment {
    public static ShulkingEnchantment enchantment;
    public static int damageOnActivate = 5;
    public static double duration = 5.0D;

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_enchantments");
    }

    @Override
    public void init() {
        enchantment = new ShulkingEnchantment(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        TreasureEnchantments.availableEnchantments.add(this);
    }

    @SubscribeEvent
    public void onAttack(LivingHurtEvent event) {
        if (!event.isCanceled()) {
            Entity trueSource = event.getSource().getTrueSource();
            LivingEntity target = event.getEntityLiving();

            if (trueSource instanceof LivingEntity && target != null) {
                LivingEntity attacker = (LivingEntity) trueSource;
                ItemStack held = attacker.getHeldItemMainhand();
                if (EnchantmentsHelper.hasEnchantment(enchantment, held)) {
                    int level = EnchantmentHelper.getEnchantmentLevel(enchantment, held);
                    target.addPotionEffect(new EffectInstance(Effects.LEVITATION, (int) (level * duration * 20), Math.max(1, level - 1)));

                    target.world.playSound(null, target.getPosition(), SoundEvents.ENTITY_SHULKER_BULLET_HIT, SoundCategory.BLOCKS, 1.0F, 0.9F);

                    if (held.attemptDamageItem(damageOnActivate, attacker.world.rand, null))
                        held.setCount(0);
                }
            }
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

        treasure.setDisplayName(new TranslationTextComponent("item.strange.treasure.shulking", treasure.getDisplayName()));
        return treasure;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
