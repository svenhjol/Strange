package svenhjol.strange.totems.item;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.totems.module.TotemOfAttracting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TotemOfAttractingItem extends MesonItem
{
    private static final String LINKED = "linked";

    public TotemOfAttractingItem(MesonModule module)
    {
        super(module, "totem_of_attracting", new Item.Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfAttracting.durability)
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
        return getLinkedBlock(stack) != null;
    }

    @Nullable
    public static Block getLinkedBlock(ItemStack stack)
    {
        String blockName = ItemNBTHelper.getString(stack, LINKED, "");
        if (blockName.isEmpty()) return null;

        Optional<Block> value = Registry.BLOCK.getValue(new ResourceLocation(blockName));
        return value.orElse(null);
    }

    public static void setLinkedBlock(ItemStack stack, Block message)
    {
        String name = Objects.requireNonNull(message.getRegistryName()).toString();
        ItemNBTHelper.setString(stack, LINKED, name);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> strings, ITooltipFlag flag)
    {
        Block block = getLinkedBlock(stack);
        if (block != null) {
            String niceName = block.getNameTextComponent().getString();

            if (!niceName.isEmpty()) {
                String message = I18n.format("totem.strange.attracting", niceName);
                if (!message.isEmpty()) {
                    strings.add(new StringTextComponent(message));
                }
            }
        }

        super.addInformation(stack, world, strings, flag);
    }
}
