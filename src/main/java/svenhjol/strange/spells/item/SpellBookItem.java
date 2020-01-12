package svenhjol.strange.spells.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.spells.block.SpellLecternBlock;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.module.SpellBooks;
import svenhjol.strange.spells.module.SpellLecterns;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;
import svenhjol.strange.spells.tile.SpellLecternTileEntity;

import javax.annotation.Nullable;
import java.util.List;

public class SpellBookItem extends MesonItem
{
    public static final String SPELL = "spell";

    public SpellBookItem(MesonModule module)
    {
        super(module, "spell_book", new Item.Properties()
            .maxStackSize(1)
            .maxDamage(16));

        addPropertyOverride(new ResourceLocation("action"), (stack, world, entity) -> {
            if (entity != null && entity.getHeldItemOffhand() == stack)
                return 1.0F;

            return 0.0F;
        });
        addPropertyOverride(new ResourceLocation("color"), (stack, world, entity) -> {
            Spell spell = getSpell(stack);
            float out = spell != null ? spell.getColor().getId() : 0;
            return out / 16.0F;
        });
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
                ItemStack book = SpellBookItem.putSpell(new ItemStack(SpellBooks.book), spell);
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

        SpellsHelper.addSpellDescription(spell, tooltip);
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

    public static ItemStack putSpell(ItemStack book, Spell spell)
    {
        if (spell == null) {
            Meson.warn("Tried to put an empty spell");
            return ItemStack.EMPTY;
        }
        book.getOrCreateTag().putString(SPELL, spell.getId()); // add new spell
        book.setDisplayName(SpellsHelper.getSpellInfoText(spell));
        return book;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = context.getItem();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();

        if (player == null) return ActionResultType.PASS;

        ItemStack book = stack.copy();
        Spell spell = SpellBookItem.getSpell(book);
        if (spell == null) return ActionResultType.PASS;

        if (state.getBlock() == Blocks.LECTERN) {
            world.removeTileEntity(pos);

            BlockState replace = SpellLecterns.block.getDefaultState()
                .with(SpellLecternBlock.FACING, state.get(LecternBlock.FACING))
                .with(SpellLecternBlock.COLOR, spell.getColor().getId());

            world.setBlockState(pos, replace, 2);

            TileEntity replaceTile = world.getTileEntity(pos);
            if (replaceTile instanceof SpellLecternTileEntity) {
                SpellLecternTileEntity spellLectern = (SpellLecternTileEntity)replaceTile;
                spellLectern.setBook(book);
                spellLectern.markDirty();
                player.getHeldItem(hand).shrink(1);
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }
}
