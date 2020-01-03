package svenhjol.strange.spells.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import svenhjol.strange.spells.item.SpellBookItem;
import svenhjol.strange.spells.module.SpellLecterns;

import javax.annotation.Nullable;

public class SpellLecternTileEntity extends TileEntity
{
    private ItemStack book = ItemStack.EMPTY;

    public SpellLecternTileEntity()
    {
        super(SpellLecterns.tile);
    }

    public void setBook(ItemStack stack)
    {
        setBook(stack, null);
    }

    public void setBook(ItemStack stack, @Nullable PlayerEntity player)
    {
        this.book = stack;
    }

    public ItemStack getBook()
    {
        return this.book;
    }

    public boolean hasBook()
    {
        return this.book.getItem() instanceof SpellBookItem;
    }

    @Override
    public void read(CompoundNBT compound)
    {
        super.read(compound);
        if (compound.contains("Book", 10)) {
            this.book = ItemStack.read(compound.getCompound("Book"));
        } else {
            this.book = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);

        if (!this.getBook().isEmpty()) {
            compound.put("Book", this.getBook().write(new CompoundNBT()));
        }

        return compound;
    }
}
