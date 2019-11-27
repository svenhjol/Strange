package svenhjol.strange.magic.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.TieredItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.IMesonItem;
import svenhjol.strange.magic.helper.MagicHelper;
import svenhjol.strange.magic.module.Spells;
import svenhjol.strange.magic.spells.Spell;

import javax.annotation.Nullable;
import java.util.List;

public class StaffItem extends TieredItem implements IMesonItem
{
    public static final String SPELL = "spells";
    public static final String USES = "quantity";
    public static final String CHARGED = "charged";
    public static final String META = "meta";

    protected float capacityMultiplier;
    protected float reloadMultiplier;

    public StaffItem(MesonModule module, String type, ItemTier material, float durabilityMultiplier)
    {
        super(material, new Properties()
            .group(ItemGroup.COMBAT)
            .maxDamage((int)(material.getMaxUses() * durabilityMultiplier)));

        this.register(module, type + "_staff");

        // some defaults
        this.capacityMultiplier = 1.0F;
        this.reloadMultiplier = 1.0F;
    }

    @Override
    public void addInformation(ItemStack staff, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
    {
        super.addInformation(staff, world, tooltip, flag);
        Spell spell = StaffItem.getSpell(staff);
        if (spell == null) return;

        ITextComponent spellText = MagicHelper.getSpellInfoText(spell);
        tooltip.add(spellText);
    }

    @Override
    public boolean onEntitySwing(ItemStack staff, LivingEntity entity)
    {
        if (!(entity instanceof PlayerEntity)) return false;
        if (entity.world.isRemote) return false;
        PlayerEntity player = (PlayerEntity)entity;

        Spell spell = StaffItem.getSpell(staff);
        if (spell == null) return false;

        StaffItem.cast(player, staff);

        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ActionResultType result = ActionResultType.SUCCESS;

        ItemStack staff = player.getHeldItem(hand);
        player.setActiveHand(hand);

        Spell spell = getSpell(staff);
        if (spell == null) return new ActionResult<>(ActionResultType.FAIL, staff);

        if (isCharged(staff)) {
            cast(player, staff);
        }

        if (!player.isCreative() && spell.getCastCost() > player.experienceTotal) {
            result = ActionResultType.FAIL;
            player.sendStatusMessage(new StringTextComponent("Not enough XP to cast the spell"), true);
        }

        return new ActionResult<>(result, staff);
    }

    @Override
    public int getUseDuration(ItemStack staff)
    {
        int duration = 20;

        Spell spell = getSpell(staff);
        if (spell != null) {
            duration = spell.getDuration() * 20; // TODO multiply by staff modifier
        }

        return duration;
    }

    @Override
    public void onUsingTick(ItemStack book, LivingEntity player, int count)
    {
        if (player != null
            && !player.world.isRemote
            && player.world.getGameTime() % 6 == 0
        ) {
            ServerWorld world = (ServerWorld) player.world;
            BlockPos pos = player.getPosition();
            Vec3d playerVec = player.getPositionVec();
            Spell spell = StaffItem.getSpell(book);
            if (spell == null) return;

            for (int i = 0; i < 1; i++) {
                double px = playerVec.x;
                double py = playerVec.y + 1.75D;
                double pz = playerVec.z;
                world.spawnParticle(Spells.enchantParticles.get(spell.getElement()), px, py, pz, 2 + (int)(count * 0.5F), 0.2D, 0.25D, 0.2D, 2.5);
            }
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
            ITextComponent message = MagicHelper.getSpellInfoText(spell, "event.strange.spellbook.activated");
            player.sendStatusMessage(message, true);

            // ding
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.getCooldownTracker().setCooldown(this, 20);
        }

        return staff;
    }

    public static CompoundNBT getMeta(ItemStack staff)
    {
        return staff.getOrCreateChildTag(META);
    }

    @Nullable
    public static Spell getSpell(ItemStack staff)
    {
        requireStaff(staff);

        String id = staff.getOrCreateTag().getString(SPELL);
        return Spells.spells.getOrDefault(id, null);
    }

    public static ItemStack putSpell(ItemStack staff, Spell spell)
    {
        requireStaff(staff);

        CompoundNBT tag = staff.getOrCreateTag();
        tag.putString(SPELL, spell.getId());
        tag.putInt(USES, spell.getQuantity());

        return staff;
    }

    public static ItemStack putCharged(ItemStack staff, boolean val)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        tag.putBoolean(CHARGED, val);
        return staff;
    }

    public static boolean isCharged(ItemStack staff)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        return tag.contains(CHARGED) && tag.getBoolean(CHARGED);
    }

    public static void decreaseUses(ItemStack staff)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        int remaining = tag.getInt(USES);

        if (remaining == 0) {
            tag.remove(SPELL);
            tag.remove(USES);
        } else {
            tag.putInt(USES, --remaining);
        }
    }

    public static ItemStack putMeta(ItemStack staff, CompoundNBT tag)
    {
        requireStaff(staff);
        CompoundNBT staffTag = staff.getOrCreateTag();
        staffTag.put(META, tag);

        return staff;
    }

    @Nullable
    public static Hand getHand(PlayerEntity player, ItemStack staff)
    {
        if (!isStaff(staff)) return null;
        return player.getHeldItemMainhand() == staff ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public static void cast(PlayerEntity player, ItemStack staff)
    {
        if (player.world.isRemote) return;

        Spell spell = getSpell(staff);
        if (spell == null) return;

        spell.cast(player, staff, result -> {
            if (result) {
                putCharged(staff, false);
                decreaseUses(staff);
                staff.damageItem(spell.getStaffDamage(), player, r -> {});
            }
        });
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
