package svenhjol.strange.magic.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.magic.helper.MagicHelper;
import svenhjol.strange.magic.module.Spells;
import svenhjol.strange.magic.spells.Spell;
import svenhjol.strange.magic.spells.Spell.Element;

import javax.annotation.Nullable;
import java.util.List;

/**
 * onItemUseFirst is delegated to {@link Spells#onStartUsingItem}
 * because Forge offers a way to change the item use duration dynamically.
 */
public class SpellBookItem extends MesonItem
{
    public static final String SPELL = "spell";
    public static final String META = "meta";
    public static final String ACTIVATED = "activated";

    public SpellBookItem(MesonModule module)
    {
        super(module, "spell_book", new Item.Properties()
            .maxDamage(24));

        // allows different item icons to be shown. Each item icon has a float ref (see model)
        addPropertyOverride(new ResourceLocation("element"), (stack, world, entity) -> {
            Spell spell = getSpell(stack);
            float out = spell != null && spell.getElement() != null ? spell.getElement().ordinal() : Element.BASE.ordinal();
            return out / 10.0F;
        });
    }

    @Override
    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
    {
        return false;
    }

    @Override
    public boolean hasEffect(ItemStack book)
    {
        return SpellBookItem.isActivated(book);
    }

    @Override
    public boolean onEntitySwing(ItemStack book, LivingEntity entity)
    {
        if (!(entity instanceof PlayerEntity)) return false;
        if (entity.world.isRemote) return false;
        PlayerEntity player = (PlayerEntity)entity;

        if (!SpellBookItem.isActivated(book)) return false;

        Spell spell = SpellBookItem.getSpell(book);
        if (spell == null) return false;

        boolean result = spell.cast(player, book);
        if (result) {
            // deactivate and damage the spellbook
            SpellBookItem.putActivated(book, false);
            book.damageItem(1, player, r -> {});
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

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ActionResultType result = ActionResultType.SUCCESS;

        ItemStack book = player.getHeldItem(hand);
        player.setActiveHand(hand);

        Spell spell = getSpell(book);
        if (spell == null) return new ActionResult<>(ActionResultType.FAIL, book);

        if (!player.isCreative() && spell.getXpCost() > player.experienceTotal) {
            result = ActionResultType.FAIL;
            player.sendStatusMessage(new StringTextComponent("Not enough XP to transfer the spell"), true);
        }

        return new ActionResult<>(result, book);
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
            Spell spell = SpellBookItem.getSpell(book);
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
    public ItemStack onItemUseFinish(ItemStack book, World world, LivingEntity entity)
    {
        if (entity instanceof PlayerEntity && !entity.world.isRemote) {
            PlayerEntity player = (PlayerEntity)entity;

            int xpCost = this.getXpCost(book);
            int xp = player.experienceTotal;
            if (!player.isCreative() && xp <= 0) return book;

            Spell spell = getSpell(book);
            if (spell == null) return book;

            if (SpellBookItem.isActivated(book)) return book;

            boolean result = spell.activate(player, book); // TODO should probably inform if this fails
            if (!result) return book;
            SpellBookItem.putActivated(book, true);

            // take XP from player
            player.giveExperiencePoints(-xpCost);

            // inform the player of the spell that was activated
            ITextComponent message = MagicHelper.getSpellInfoText(spell, "event.strange.spellbook.activated");
            player.sendStatusMessage(message, true);

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
            for (String id : Spells.spells.keySet()) {
                Spell spell = Spells.spells.get(id);
                ItemStack book = SpellBookItem.setSpell(new ItemStack(Spells.item), spell);
                book.setDisplayName(MagicHelper.getSpellInfoText(spell));
                items.add(book);
            }
        }
    }

    @Override
    public void addInformation(ItemStack book, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
    {
        super.addInformation(book, world, tooltip, flag);
        Spell spell = SpellBookItem.getSpell(book);
        if (spell == null) return;

        ITextComponent descriptionText = new TranslationTextComponent(spell.getDescriptionKey());
        descriptionText.setStyle((new Style()).setColor(TextFormatting.WHITE));

        ITextComponent affectText = new TranslationTextComponent(spell.getAffectKey());
        affectText.setStyle((new Style()).setColor(TextFormatting.GRAY));

        tooltip.add(descriptionText);
        tooltip.add(affectText);
    }

    public int getXpCost(ItemStack book)
    {
        Spell spell = getSpell(book);
        return spell != null ? spell.getXpCost() : 0;
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

        if (Spells.spells.containsKey(id)) {
            return Spells.spells.get(id);
        }

        return null;
    }

    public static CompoundNBT getMeta(ItemStack book)
    {
        return book.getOrCreateChildTag(META);
    }

    public static ItemStack putMeta(ItemStack book, CompoundNBT tag)
    {
        CompoundNBT bookTag = book.getOrCreateTag();
        bookTag.put(META, tag);

        return book;
    }

    public static ItemStack putActivated(ItemStack book, boolean val)
    {
        CompoundNBT tag = book.getOrCreateTag();
        tag.putBoolean(ACTIVATED, val);
        return book;
    }

    public static boolean isActivated(ItemStack book)
    {
        CompoundNBT tag = book.getOrCreateTag();
        return tag.contains(ACTIVATED) && tag.getBoolean(ACTIVATED);
    }
}
