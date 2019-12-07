package svenhjol.strange.spells.item;

import com.google.gson.JsonParseException;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.IMesonItem;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;

import javax.annotation.Nullable;
import java.util.List;

public class StaffItem extends TieredItem implements IMesonItem
{
    public static final String SPELL = "spells";
    public static final String USES = "quantity";
    public static final String CHARGED = "charged";
    public static final String META = "meta";
    public static final String ORIGINAL_NAME = "originalName";

    protected float capacityMultiplier;
    protected float durationMultiplier;
    protected float attackDamage;

    public StaffItem(MesonModule module, String type, ItemTier material, float durabilityMultiplier)
    {
        super(material, new Properties()
            .group(ItemGroup.COMBAT)
            .maxDamage((int)(material.getMaxUses() * durabilityMultiplier)));

        this.register(module, type + "_staff");

        // some defaults
        this.setCapacityMultiplier(1.0F);
        this.setDurationMultiplier(1.0F);
    }

    @Override
    public void addInformation(ItemStack staff, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
    {
        super.addInformation(staff, world, tooltip, flag);
        Spell spell = StaffItem.getSpell(staff);
        if (spell == null) return;
        tooltip.add(new TranslationTextComponent("staff.strange.uses", StaffItem.getUses(staff)));
    }

    @Override
    public boolean hasEffect(ItemStack staff)
    {
        return StaffItem.isCharged(staff);
    }

    @Override
    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
    {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack staff, LivingEntity entity)
    {
        if (!(entity instanceof PlayerEntity)) return false;
        if (entity.world.isRemote) return false;
        PlayerEntity player = (PlayerEntity)entity;

        Spell spell = StaffItem.getSpell(staff);
        if (spell == null) return false;

        if (!StaffItem.isCharged(staff)) return false;
        StaffItem.cast(player, staff);

        return false;
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker)
    {
        target.attackEntityFrom(DamageSource.MAGIC, 4.0F);
        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ActionResultType result = ActionResultType.SUCCESS;

        ItemStack staff = player.getHeldItem(hand);
        Spell spell = getSpell(staff);
        if (spell == null) return new ActionResult<>(ActionResultType.FAIL, staff);

        if (isCharged(staff)) {
            if (player.getHeldItemOffhand().getItem() instanceof StaffItem
                && !(player.getHeldItemMainhand().getItem() instanceof StaffItem)
            ) {
                // if charged then cast the spell - this is required for staff being in offhand
                result = ActionResultType.PASS;
                cast(player, staff);
            } else {
                result = ActionResultType.FAIL;
            }
        } else if (!player.isCreative() && spell.getCastCost() > player.experienceTotal) {
            // check there's enough XP to charge the staff
            result = ActionResultType.FAIL;
            player.sendStatusMessage(new TranslationTextComponent("event.strange.spellbook.not_enough_xp"), true);
        }

        if (result == ActionResultType.SUCCESS) {
            player.setActiveHand(hand);
            SoundEvent sound;

            if (spell.getDuration() < 1.5F) {
                sound = StrangeSounds.STAFF_CHARGE_SHORT;
            } else if (spell.getDuration() < 3.0F) {
                sound = StrangeSounds.STAFF_CHARGE_MEDIUM;
            } else {
                sound = StrangeSounds.STAFF_CHARGE_LONG;
            }

            player.world.playSound(null, player.getPosition(), sound, SoundCategory.PLAYERS, 0.5F, 1.0F);
        }

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
        int duration = 20;

        Spell spell = getSpell(staff);
        if (spell != null) {
            duration = (int)(spell.getDuration() * 20 * ((StaffItem)staff.getItem()).getDurationMultiplier());
        }

        return duration;
    }

    @Override
    public void onUsingTick(ItemStack book, LivingEntity player, int count)
    {
        if (player != null
            && !player.world.isRemote
            && player.world.getGameTime() % 5 == 0
        ) {
            Spell spell;
            ItemStack heldMain = player.getHeldItemMainhand();

            if (heldMain.getItem() instanceof StaffItem) {
                spell = StaffItem.getSpell(heldMain);
            } else {
                spell = StaffItem.getSpell(book);
            }

            if (spell == null) return;

            Spells.effectEnchantStaff((ServerPlayerEntity)player, spell, 2 + (int)(count * 0.5D), 0.2D, 0.2D, 0.2, 2.2D);
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack staff, World world, LivingEntity entity)
    {
        if (entity instanceof PlayerEntity && !entity.world.isRemote) {
            PlayerEntity player = (PlayerEntity)entity;

            Spell spell = getSpell(staff);
            if (spell == null) return staff;

            int xpCost = spell.getCastCost();
            int xp = player.experienceTotal;
            if (!player.isCreative() && xp <= 0) return staff;

            if (isCharged(staff)) return staff;

            boolean result = spell.activate(player, staff); // TODO should probably inform if this fails
            if (!result) return staff;
            putCharged(staff, true);

            // take XP from player
            player.giveExperiencePoints(-xpCost);

            // inform the player of the spell that was activated
            ITextComponent message = SpellsHelper.getSpellInfoText(spell, "event.strange.spellbook.activated");
            player.sendStatusMessage(message, true);

            // ding
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.getCooldownTracker().setCooldown(this, 20);
        }

        return staff;
    }

    public float getCapacityMultiplier()
    {
        return capacityMultiplier;
    }

    public float getDurationMultiplier()
    {
        return durationMultiplier;
    }

    public StaffItem setCapacityMultiplier(float capacityMultiplier)
    {
        this.capacityMultiplier = capacityMultiplier;
        return this;
    }

    public StaffItem setDurationMultiplier(float durationMultiplier)
    {
        this.durationMultiplier = durationMultiplier;
        return this;
    }

    public StaffItem setAttackDamage(float attackDamage)
    {
        this.attackDamage = attackDamage;
        return this;
    }

    public static int getUses(ItemStack staff)
    {
        return staff.getOrCreateTag().getInt(USES);
    }

    public static CompoundNBT getMeta(ItemStack staff)
    {
        return staff.getOrCreateChildTag(META);
    }

    public static ITextComponent getOriginalName(ItemStack staff)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        if (tag.contains(ORIGINAL_NAME)) {
            try {
                ITextComponent name = ITextComponent.Serializer.fromJson(tag.getString(ORIGINAL_NAME));
                if (name != null) {
                    return name;
                }
            } catch (JsonParseException e) {
                tag.remove(ORIGINAL_NAME);
            }
        }
        return new TranslationTextComponent(staff.getTranslationKey());
    }

    public static void putOriginalName(ItemStack staff, ITextComponent name)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        tag.putString(ORIGINAL_NAME, ITextComponent.Serializer.toJson(name));
    }

    @Nullable
    public static Spell getSpell(ItemStack staff)
    {
        requireStaff(staff);

        String id = staff.getOrCreateTag().getString(SPELL);
        return Spells.spells.getOrDefault(id, null);
    }

    public static void putSpell(ItemStack staff, Spell spell)
    {
        requireStaff(staff);

        CompoundNBT tag = staff.getOrCreateTag();
        tag.putString(SPELL, spell.getId());
        putUses(staff, (int)(spell.getQuantity() * ((StaffItem)staff.getItem()).getCapacityMultiplier()));

        ITextComponent originalName = staff.getDisplayName();
        StaffItem.putOriginalName(staff, originalName);

        staff.setDisplayName(new TranslationTextComponent("staff.strange.named_spell", originalName.getFormattedText(), SpellsHelper.getSpellInfoText(spell).getFormattedText()));
    }

    public static void putUses(ItemStack staff, int uses)
    {
        staff.getOrCreateTag().putInt(USES, uses);
    }

    public static void putCharged(ItemStack staff, boolean val)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        tag.putBoolean(CHARGED, val);
    }

    public static void putMeta(ItemStack staff, CompoundNBT tag)
    {
        requireStaff(staff);
        CompoundNBT staffTag = staff.getOrCreateTag();
        staffTag.put(META, tag);
    }

    public static void cast(PlayerEntity player, ItemStack staff)
    {
        if (player.world.isRemote) return;

        Spell spell = getSpell(staff);
        if (spell == null) return;

        putCharged(staff, false); // until the spell returns
        player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_CAST, SoundCategory.PLAYERS, 1.0F, 1.0F);

        spell.cast(player, staff, result -> {
            if (result) {
                boolean hasUses = decreaseUses(staff);
                staff.damageItem(spell.getStaffDamage(), player, r -> {});
                if (!hasUses) {
                    player.world.playSound(null, player.getPosition(), StrangeSounds.STAFF_EMPTY, SoundCategory.PLAYERS, 0.75F, 1.0F);
                }
            } else {
                // wasn't successful, set back to charged
                putCharged(staff, true);
                player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        });

        player.getCooldownTracker().setCooldown(staff.getItem(), 5);
    }

    public static boolean isCharged(ItemStack staff)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        return tag.contains(CHARGED) && tag.getBoolean(CHARGED);
    }

    public static boolean decreaseUses(ItemStack staff)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        int remaining = tag.getInt(USES) - 1;

        if (remaining <= 0) {
            clear(staff);
            return false;
        } else {
            tag.putInt(USES, remaining);
            return true;
        }
    }

    public static void clear(ItemStack staff)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        tag.remove(SPELL);
        tag.remove(META);
        tag.remove(USES);
        tag.remove(CHARGED);
        staff.setDisplayName(StaffItem.getOriginalName(staff));
    }

    public static boolean isStaff(ItemStack staff)
    {
        return staff.getItem() instanceof StaffItem;
    }

    public static boolean hasSpell(ItemStack staff)
    {
        return StaffItem.getSpell(staff) instanceof Spell;
    }

    public static void requireStaff(ItemStack staff)
    {
        if (!isStaff(staff)) {
            throw new RuntimeException("Trying to add spells to something that isn't a staff is illegal by decree of the Ministry.");
        }
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
