package svenhjol.strange.spells.item;

import com.google.common.collect.Sets;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.EnchantmentsHelper;
import svenhjol.meson.iface.IMesonItem;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.module.Staves;
import svenhjol.strange.spells.spells.Spell;

import javax.annotation.Nullable;

public class StaffItem extends ToolItem implements IMesonItem
{
    public StaffItem(MesonModule module, String type, IItemTier tier)
    {
        super(tier.getAttackDamage() + 2.0F, -1.0F, tier, Sets.newHashSet(), new Item.Properties()
            .group(ItemGroup.TOOLS));

        this.register(module, type + "_staff");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ActionResultType result = ActionResultType.SUCCESS;
        ItemStack staff = player.getHeldItem(hand);

        // get spell from held spellbook
        Spell spell = getSpellFromBook(player);
        if (spell == null) {
            return new ActionResult<>(ActionResultType.FAIL, staff);
        }

        int cost = spell.getApplyCost();
        int xp = player.experienceTotal;
        int levels = player.experienceLevel;

        if (!player.isCreative()) {
            if (xp <= 0 || levels - cost < 0) {
                player.sendStatusMessage(SpellsHelper.getSpellInfoText(spell, "event.strange.spellbook.not_enough_xp"), true);
                return new ActionResult<>(ActionResultType.FAIL, staff);
            }
        }

        player.setActiveHand(hand);

        int level = getEfficiency(staff);
        SoundEvent sound = level < 3 ? StrangeSounds.SPELL_CHARGE_MEDIUM : StrangeSounds.SPELL_CHARGE_SHORT;
        player.world.playSound(null, player.getPosition(), sound, SoundCategory.PLAYERS, 0.5F, 0.8F + (level * 0.1F));
        return new ActionResult<>(result, staff);
    }

    @Override
    public UseAction getUseAction(ItemStack staff)
    {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack staff)
    {
        int level = getEfficiency(staff);
        return 30 - (level * 5);
    }

    @Override
    public void onUsingTick(ItemStack staff, LivingEntity player, int count)
    {
        if (player != null
            && !player.world.isRemote
            && player.world.getGameTime() % 5 == 0
        ) {
            Spell spell = getSpellFromBook(player);
            if (spell == null) return;

            Staves.effectEnchantStaff((ServerPlayerEntity) player, spell, 2 + (int) (count * 0.5D), 0.2D, 0.2D, 0.2, 2.2D);
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack staff, World world, LivingEntity entity)
    {
        if (!entity.world.isRemote) {
            Spell spell = getSpellFromBook(entity);
            if (spell == null) return staff;

            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;

                final int cost = spell.getApplyCost();
                final int xp = player.experienceTotal;
                final int levels = player.experienceLevel;

                if (!player.isCreative()) {
                    if (xp <= 0 || levels - cost < 0) {
                        player.sendStatusMessage(SpellsHelper.getSpellInfoText(spell, "event.strange.spellbook.not_enough_xp"), true);
                        return staff;
                    }
                }

                // check for activated spells first
                boolean activated = Spells.activate(player, staff, spell);
                if (activated) return staff;

                Spells.cast(player, staff, spell, used -> {
                    if (!player.isCreative()) {
                        int xpCost = cost;
                        boolean damageBook = true;

                        int fortune = getFortune(staff);
                        int efficiency = getEfficiency(staff);

                        if (world.rand.nextFloat() < fortune * 0.2F)
                            xpCost = Math.min(0, xpCost - fortune);

                        if (world.rand.nextFloat() < efficiency * 0.1F)
                            damageBook = false;

                        if (xpCost > 0)
                            player.addExperienceLevel(-xpCost);

                        // damage spell book - efficiency decreases chance
                        ItemStack book = player.getHeldItemOffhand();
                        if (damageBook && book.attemptDamageItem(1, world.rand, (ServerPlayerEntity) player)) {
                            book.shrink(1);
                            player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_NO_MORE_USES, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            player.sendBreakAnimation(EquipmentSlotType.OFFHAND);
                        }

                        // damage staff - unbreaking decreases chance
                        if (staff.attemptDamageItem(1, world.rand, (ServerPlayerEntity) player)) {
                            staff.shrink(1);
                            player.sendBreakAnimation(EquipmentSlotType.MAINHAND);
                        }
                    }
                });
            }
        }

        return staff;
    }

    @Nullable
    public Spell getSpellFromBook(LivingEntity player)
    {
        if (!(player.getHeldItemOffhand().getItem() instanceof SpellBookItem)) {
            return null;
        }
        ItemStack book = player.getHeldItemOffhand();
        return SpellBookItem.getSpell(book);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    public int getEfficiency(ItemStack staff)
    {
        int level = 0;

        if (EnchantmentsHelper.hasEnchantment(Enchantments.EFFICIENCY, staff))
            level = EnchantmentHelper.getEnchantments(staff).get(Enchantments.EFFICIENCY);

        return level;
    }

    public int getFortune(ItemStack staff)
    {
        int level = 0;

        if (EnchantmentsHelper.hasEnchantment(Enchantments.FORTUNE, staff))
            level = EnchantmentHelper.getEnchantments(staff).get(Enchantments.FORTUNE);

        return level;
    }
}
