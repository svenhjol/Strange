package svenhjol.strange.magic.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
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
    public boolean onEntitySwing(ItemStack book, LivingEntity entity)
    {
        if (!(entity instanceof PlayerEntity)) return false;

        PlayerEntity player = (PlayerEntity)entity;

        Spell spell = SpellBookItem.getSpell(book);
        if (spell == null) return false;

        boolean result = spell.cast(player); // TODO should do something with result here

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

        if (!player.isCreative()) {
            if (spell == null || spell.getXpCost() > player.experienceTotal) {
                result = ActionResultType.FAIL;
                player.sendStatusMessage(new StringTextComponent("Not enough XP to transfer the spell"), true);
            }
        }

        return new ActionResult<>(result, book);
    }

    @Override
    public void onUsingTick(ItemStack book, LivingEntity player, int count)
    {
        if (player != null
            && !player.world.isRemote
            && player.world.getGameTime() % 5 == 0
        ) {
            SpellBookItem.effectUseBook((ServerPlayerEntity)player, SpellBookItem.getSpell(book));
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

            boolean result = spell.activate(player); // TODO should probably inform if this fails
            if (!result) return book;

            // damage the spellbook
            book.damageItem(1, player, r -> effectUseBook((ServerPlayerEntity)player, spell));

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

    public static void effectUseBook(ServerPlayerEntity player, Spell spell)
    {
        ServerWorld world = (ServerWorld) player.world;
        BlockPos pos = player.getPosition();

        double spread = 1.25D;
        for (int i = 0; i < 1; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            world.spawnParticle(Spells.enchantParticles.get(spell.getElement()), px, py, pz, 10, 0.0D, 0.5D, 0.0D, 2.2);
        }
    }
}
