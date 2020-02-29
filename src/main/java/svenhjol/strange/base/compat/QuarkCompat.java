package svenhjol.strange.base.compat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.DyeColor;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.enums.WoodType;
import svenhjol.meson.helper.ItemNBTHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.building.module.VariantChestsModule;
import vazkii.quark.tools.module.AncientTomesModule;
import vazkii.quark.vanity.item.RuneItem;
import vazkii.quark.vanity.module.ColorRunesModule;
import vazkii.quark.world.block.CaveCrystalBlock;
import vazkii.quark.world.module.BigDungeonModule;
import vazkii.quark.world.module.CaveRootsModule;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuarkCompat
{
    public boolean hasModule(ResourceLocation res)
    {
        return Meson.isModuleEnabled(res);
    }

    public ItemStack getRandomAncientTome(Random rand)
    {
        List<Enchantment> validEnchants = AncientTomesModule.validEnchants;
        ItemStack tome = new ItemStack(AncientTomesModule.ancient_tome);

        Enchantment enchantment = validEnchants.get(rand.nextInt(validEnchants.size()));
        EnchantedBookItem.addEnchantment(tome, new EnchantmentData(enchantment, enchantment.getMaxLevel()));

        return tome;
    }

    public boolean isInsideBigDungeon(ServerWorld world, BlockPos pos)
    {
        return BigDungeonModule.structure.isPositionInsideStructure(world, pos);
    }

    public boolean isCrystal(World world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof CaveCrystalBlock;
    }

    public boolean hasCaveRoots()
    {
        return ModuleLoader.INSTANCE.isModuleEnabled(CaveRootsModule.class);
    }

    public boolean hasVariantChests()
    {
        return ModuleLoader.INSTANCE.isModuleEnabled(VariantChestsModule.class);
    }

    public boolean hasBigDungeons()
    {
        return ModuleLoader.INSTANCE.isModuleEnabled(BigDungeonModule.class);
    }

    public String getBigDungeonResName()
    {
        if (BigDungeonModule.structure != null) {
            return BigDungeonModule.structure.getStructureName();
        } else {
            return "quark:big_dungeon";
        }
    }

    public Structure<?> getBigDungeonStructure()
    {
        return BigDungeonModule.structure;
    }

    @Nullable
    public Block getCaveRootBlock()
    {
        if (hasCaveRoots())
            return CaveRootsModule.root;

        return null;
    }

    @Nullable
    public Block getRandomChest(Random rand)
    {
        List<WoodType> types = Arrays.asList(WoodType.values());
        WoodType type = types.get(rand.nextInt(types.size()));
        ResourceLocation res = new ResourceLocation(Quark.MOD_ID, type.name().toLowerCase() + "_chest");
        return ForgeRegistries.BLOCKS.getValue(res);
    }

    public boolean hasColorRuneModule()
    {
        return ModuleLoader.INSTANCE.isModuleEnabled(ColorRunesModule.class);
    }

    public void applyColor(ItemStack stack, DyeColor color)
    {
        // get the rune
        Item runeItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Quark.MOD_ID, color.getName() + "_rune"));
        if (runeItem instanceof RuneItem) {
            ItemStack rune = new ItemStack(runeItem);
            ItemNBTHelper.setBoolean(stack, ColorRunesModule.TAG_RUNE_ATTACHED, true);
            ItemNBTHelper.setCompound(stack, ColorRunesModule.TAG_RUNE_COLOR, rune.serializeNBT());
        }
    }
}
