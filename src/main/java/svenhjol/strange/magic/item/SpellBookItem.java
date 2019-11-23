package svenhjol.strange.magic.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.magic.module.SpellBooks;
import svenhjol.strange.magic.spells.Spell;
import svenhjol.strange.magic.spells.Spell.Element;

import javax.annotation.Nullable;

public class SpellBookItem extends MesonItem
{
    public static final String SPELL = "spell";

    public SpellBookItem(MesonModule module)
    {
        super(module, "spell_book", new Item.Properties()
            .maxDamage(64));

        addPropertyOverride(new ResourceLocation("element"), (stack, world, entity) -> {
            Spell spell = getSpell(stack);
            float out = spell != null && spell.getElement() != null ? spell.getElement().ordinal() : Element.NONE.ordinal();
            return out / 10.0F;
        });
    }

    @Override
    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
    {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack book, LivingEntity entity)
    {
        if (!(entity instanceof PlayerEntity)) return false;

        PlayerEntity player = (PlayerEntity)entity;
        ItemStack staff = SpellBookItem.getStaffInOtherHand(player, book);
        if (staff == null) return false;

        if (StaffItem.castable(staff, player.world.getGameTime())) {
            StaffItem.cast(player, staff);
        }

        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack book, BlockPos pos, PlayerEntity player)
    {
        return false;
    }

    @Override
    public UseAction getUseAction(ItemStack book)
    {
        return UseAction.NONE;
    }

    @Override
    public int getUseDuration(ItemStack book)
    {
        Spell spell = getSpell(book);
        return spell != null ? spell.getDuration() : 20;
    }

    public int getXpCost(ItemStack book)
    {
        Spell spell = getSpell(book);
        return spell != null ? spell.getXpCost() : 0;
    }

//    @Override
//    public ActionResultType onItemUse(ItemUseContext context)
//    {
//        return super.onItemUse(context);
//    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ActionResultType result = ActionResultType.SUCCESS;

        ItemStack book = player.getHeldItem(hand);
        player.setActiveHand(hand);

        Spell spell = getSpell(book);
        if (spell == null || spell.getXpCost() > player.experienceTotal) {
            result = ActionResultType.FAIL;
            player.sendStatusMessage(new StringTextComponent("Not enough XP to transfer the spell"), true);
        }

        return new ActionResult<>(result, book);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack book, World world, LivingEntity entity)
    {
        if (entity instanceof PlayerEntity && !entity.world.isRemote) {
            PlayerEntity player = (PlayerEntity)entity;

            int xp = player.experienceTotal;
            if (xp <= 0) return book;

            ItemStack staff = getStaffInOtherHand(player, book);
            if (staff == null) return book;

            int xpCost = this.getXpCost(book);

            // take XP from player
            player.giveExperiencePoints(-xpCost);

            // set staff spell
            Spell spell = getSpell(book);
            if (spell == null) return book;

            StaffItem.pushSpell(staff, spell);
            spell.transfer(player, staff);

            // damage the spellbook
            book.damageItem(1, player, r -> effectUseBook((ServerPlayerEntity)player));

            // ding
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.getCooldownTracker().setCooldown(this, 20);
        }

        return book;
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        Spell spell = getSpell(stack);
        return spell != null ? spell.getTranslationKey() : this.getTranslationKey();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        if (group == ItemGroup.SEARCH) {
            for (String id : SpellBooks.spells.keySet()) {
                Spell spell = SpellBooks.spells.get(id);
                ItemStack book = SpellBookItem.setSpell(new ItemStack(SpellBooks.item), spell);
                items.add(book);
            }
        }
    }

    public static ItemStack setSpell(ItemStack book, Spell spell)
    {
        book.getOrCreateTag().putString(SPELL, spell.getId()); // add new spell
        return book;
    }

    @Nullable
    public static Spell getSpell(ItemStack book)
    {
        String id = book.getOrCreateTag().getString(SPELL);

        if (SpellBooks.spells.containsKey(id)) {
            return SpellBooks.spells.get(id);
        }

        return null;
    }

    @Nullable
    public static ItemStack getStaffInOtherHand(PlayerEntity player, ItemStack book)
    {
        Hand staffHand = player.getHeldItemMainhand() == book ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack staff = player.getHeldItem(staffHand);
        if (!(staff.getItem() instanceof StaffItem)) return null;
        return staff;
    }

    public static void effectUseBook(ServerPlayerEntity player)
    {
        ServerWorld world = (ServerWorld) player.world;
        BlockPos pos = player.getPosition();

        double spread = 1.5D;
        for (int i = 0; i < 1; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            world.spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 5, 0.0D, 0.5D, 0.0D, 3);
        }
    }
}
