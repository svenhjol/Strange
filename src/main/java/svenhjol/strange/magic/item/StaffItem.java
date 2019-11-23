package svenhjol.strange.magic.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.IMesonItem;
import svenhjol.strange.magic.module.SpellBooks;
import svenhjol.strange.magic.spells.Spell;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class StaffItem extends TieredItem implements IMesonItem
{
    public static final String SPELLS = "spells";
    public static final String META = "meta";
    public static final String LAST_USED = "lastUsed";

    protected int capacity; // number of spells that it can hold
    protected float transferMultiplier; // speed at which spells are transferred to the staff

    public StaffItem(MesonModule module, String type, ItemTier material, float durabilityMultiplier)
    {
        super(material, new Item.Properties()
            .group(ItemGroup.COMBAT)
            .maxDamage((int)(material.getMaxUses() * durabilityMultiplier)));

        this.register(module, type + "_staff");

        // some defaults
        this.capacity = 1;
        this.transferMultiplier = 1.0F;
    }

    public float getTransferMultiplier()
    {
        return transferMultiplier;
    }

    public int getCapacity()
    {
        return capacity;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        return super.onItemUse(context);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public boolean hasEffect(ItemStack staff)
    {
        return getNumberOfSpells(staff) > 0;
    }

    public StaffItem setCapacity(int capacity)
    {
        this.capacity = capacity;
        return this;
    }

    public StaffItem setTransferMultiplier(float transferMultiplier)
    {
        this.transferMultiplier = transferMultiplier;
        return this;
    }

    public static CompoundNBT getSpellMeta(ItemStack staff, int index)
    {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT meta = staff.getOrCreateChildTag(META);
        String sindex = String.valueOf(index);
        if (meta.contains(sindex)) {
            tag = (CompoundNBT)meta.get(sindex);
        }
        return tag;
    }

    public static List<Spell> getSpells(ItemStack staff)
    {
        checkStaff(staff);

        List<Spell> out = new ArrayList<>();

        CompoundNBT spellsTag = staff.getChildTag(SPELLS);
        if (spellsTag == null) return out;

        for (String sindex : spellsTag.keySet()) {
            String id = spellsTag.getString(sindex);

            if (SpellBooks.spells.containsKey(id)) {
                out.add(SpellBooks.spells.get(id));
            }
        }

        return out;
    }

    public static ItemStack putSpells(ItemStack staff, List<Spell> spells)
    {
        checkStaff(staff);

        int index = 0;
        CompoundNBT spellsTag = new CompoundNBT();

        for (Spell spell : spells) {
            spellsTag.putString(String.valueOf(index++), spell.getId());
        }

        staff.getOrCreateTag().put(SPELLS, spellsTag);
        return staff;
    }

    public static ItemStack putSpellMeta(ItemStack staff, int index, CompoundNBT tag)
    {
        checkStaff(staff);

        CompoundNBT meta = staff.getOrCreateChildTag(META);
        meta.put(String.valueOf(index), tag);

        return staff;
    }

    public static int getCapacity(ItemStack staff)
    {
        checkStaff(staff);

        return ((StaffItem)staff.getItem()).getCapacity();
    }

    public static int getNumberOfSpells(ItemStack staff)
    {
        CompoundNBT childTag = staff.getChildTag(SPELLS);
        if (childTag == null) return 0;
        return childTag.size();
    }

    @Nullable
    public static ItemStack pushSpell(ItemStack staff, Spell spell)
    {
        List<Spell> spells = getSpells(staff);
        int capacity = getCapacity(staff);
        spells.add(spell); // push

        if (spells.size() > capacity) {
            spells.remove(0); // shift
        }

        return putSpells(staff, spells);
    }

    @Nullable
    public static Spell popSpell(ItemStack staff)
    {
        List<Spell> spells = getSpells(staff);
        if (spells.size() > 0) {
            Spell spell = spells.remove(spells.size() - 1);
            putSpells(staff, spells);
            return spell;
        }
        return null;
    }

    public static Hand getHand(PlayerEntity player, ItemStack staff)
    {
        StaffItem.checkStaff(staff);
        return player.getHeldItemMainhand() == staff ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    /**
     * Called when the player uses the staff.
     * @param player
     * @param staff
     * @return
     */
    public static boolean cast(PlayerEntity player, ItemStack staff)
    {
        if (player.world.isRemote) return false;

        Spell spell = popSpell(staff);
        if (spell == null) return false;

        boolean result = spell.cast(player, staff);
        if (!result) {
            // put the spell back if it failed to cast
            pushSpell(staff, spell);
        }
        return result;
    }

    public static void checkStaff(ItemStack staff)
    {
        if (!(staff.getItem() instanceof StaffItem)) {
            throw new RuntimeException("Trying to add spells to something that isn't a staff is illegal by decree of the Ministry.");
        }
    }

    public static boolean castable(ItemStack staff, long time)
    {
        CompoundNBT tag = staff.getOrCreateTag();
        if (tag.contains(LAST_USED)) {
            long last = tag.getLong(LAST_USED);
            if (last == 0 || (time - last) > 20) {
                tag.putLong(LAST_USED, time);
                return true;
            }
        } else {
            tag.putLong(LAST_USED, time);
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
