package svenhjol.strange.spells.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.module.Moonstones;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;
import vazkii.quark.api.IRuneColorProvider;
import vazkii.quark.api.QuarkCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MoonstoneItem extends MesonItem implements IRuneColorProvider
{
    public static final String SPELL = "spells";

    public MoonstoneItem(MesonModule module)
    {
        super(module, "moonstone", new Item.Properties()
            .group(ItemGroup.TOOLS)
            .maxStackSize(1)
        );

        // allows different item icons to be shown. Each item icon has a float ref (see model)
        addPropertyOverride(new ResourceLocation("color"), (stone, world, entity) -> {
            Spell spell = getSpell(stone);
            float out = spell != null ? spell.getColor().getId() : 0;
            return out / 16.0F;
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack stone = player.getHeldItem(hand);
        Spell spell = getSpell(stone);
        if (spell == null) return new ActionResult<>(ActionResultType.FAIL, stone);

        Spells.activate(player, stone, spell);
        Spells.cast(player, stone, spell, used -> {
            if (used) stone.shrink(1);
        });
        return new ActionResult<>(ActionResultType.SUCCESS, stone);
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return Moonstones.glint && hasSpell(stack);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Nullable
    public static Spell getSpell(ItemStack stone)
    {
        String id = stone.getOrCreateTag().getString(SPELL);
        return Spells.spells.getOrDefault(id, null);
    }

    public static boolean hasSpell(ItemStack stone)
    {
        return getSpell(stone) instanceof Spell;
    }

    public static void putSpell(ItemStack stone, Spell spell)
    {
        CompoundNBT tag = stone.getOrCreateTag();
        tag.putString(SPELL, spell.getId());
        Spells.putUses(stone, spell.getUses());

        // change the stone name
        stone.setDisplayName(SpellsHelper.getSpellInfoText(spell));
    }

    @Override
    public void addInformation(ItemStack stone, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
    {
        super.addInformation(stone, world, tooltip, flag);
        Spell spell = getSpell(stone);
        if (spell == null) return;

        SpellsHelper.addSpellDescription(spell, tooltip);

        TranslationTextComponent usesText = new TranslationTextComponent("moonstone.strange.uses", Spells.getUses(stone));
        usesText.setStyle((new Style()).setColor(TextFormatting.YELLOW));
        tooltip.add(usesText);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getRuneColor(ItemStack stack)
    {
        Spell spell = getSpell(stack);
        if (spell == null) return 0;

        float[] components = spell.getColor().getColorComponentValues();
        return 0xFF000000 |
            ((int) (255 * components[0]) << 16) |
            ((int) (255 * components[1]) << 8) |
            (int) (255 * components[2]);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        final LazyOptional<IRuneColorProvider> holder = LazyOptional.of(() -> this);

        return new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return QuarkCapabilities.RUNE_COLOR.orEmpty(cap, holder);
            }
        };
    }
}
