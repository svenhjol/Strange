package svenhjol.strange.totems.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.base.UtilHelper;
import svenhjol.strange.totems.module.TotemOfExtracting;

import javax.annotation.Nullable;
import java.util.List;

public class TotemOfExtractingItem extends MesonItem
{
    private static final String POS = "pos";
    private static final String DIM = "dim";
    private static final String BLOCKNAME = "blockname";

    public TotemOfExtractingItem(MesonModule module)
    {
        super(module, "totem_of_extracting", new Properties()
            .group(ItemGroup.TRANSPORTATION)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfExtracting.durability)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return getPos(stack) != null;
    }

    public static void clearTags(ItemStack stack)
    {
        stack.removeChildTag(POS);
        stack.removeChildTag(DIM);
        stack.removeChildTag(BLOCKNAME);
    }

    @Nullable
    public static BlockPos getPos(ItemStack stack)
    {
        long pos = ItemNBTHelper.getLong(stack, POS, 0);
        return pos == 0 ? null : BlockPos.fromLong(pos);
    }

    public static int getDim(ItemStack stack)
    {
        return ItemNBTHelper.getInt(stack, DIM, 0);
    }

    public static String getBlockName(ItemStack stack)
    {
        return ItemNBTHelper.getString(stack, BLOCKNAME, "");
    }

    public static void setPos(ItemStack stack, BlockPos pos)
    {
        ItemNBTHelper.setLong(stack, POS, pos.toLong());
    }

    public static void setDim(ItemStack stack, int dim)
    {
        ItemNBTHelper.setInt(stack, DIM, dim);
    }

    public static void setBlockName(ItemStack stack, String message)
    {
        ItemNBTHelper.setString(stack, BLOCKNAME, message);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> strings, ITooltipFlag flag)
    {
        String name = getBlockName(stack);

        if (!name.isEmpty()) {
            String message = I18n.format("totem.strange.extracting", getBlockName(stack), UtilHelper.formatBlockPos(getPos(stack)));
            if (!message.isEmpty()) {
                strings.add(new StringTextComponent(message));
            }
        }

        super.addInformation(stack, world, strings, flag);
    }
}
