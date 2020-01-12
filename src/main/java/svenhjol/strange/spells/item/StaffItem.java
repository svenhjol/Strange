package svenhjol.strange.spells.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.IMesonItem;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.module.Staves;
import svenhjol.strange.spells.spells.Spell;

import javax.annotation.Nullable;

public class StaffItem extends TieredItem implements IMesonItem
{
    public StaffItem(MesonModule module, String type, IItemTier tier)
    {
        super(tier, new Item.Properties()
            .group(ItemGroup.COMBAT));

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
        player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_CHARGE_SHORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
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
        return 18;
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

                int cost = spell.getApplyCost();
                int xp = player.experienceTotal;
                int levels = player.experienceLevel;

                if (!player.isCreative()) {
                    if (xp <= 0 || levels - cost < 0) {
                        player.sendStatusMessage(SpellsHelper.getSpellInfoText(spell, "event.strange.spellbook.not_enough_xp"), true);
                        return staff;
                    }
                }

                // check for activated spells first
                boolean activated = Spells.activate(player, staff, spell);
                if (activated) return staff;

                Spells.cast(player, staff, spell, s -> {
                    // take XP from player
                    if (!player.isCreative()) {
                        player.addExperienceLevel(-cost);

                        // damage spell book
                        ItemStack book = player.getHeldItemOffhand();
                        if (book.attemptDamageItem(1, world.rand, (ServerPlayerEntity) player)) {
                            book.shrink(1);
                        }

                        // damage staff
                        s.attemptDamageItem(1, world.rand, (ServerPlayerEntity) player);
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
}
