package svenhjol.strange.spells.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import svenhjol.meson.helper.StringHelper;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SpellsHelper
{
    public static ITextComponent getSpellInfoText(Spell spell)
    {
        return getSpellInfoText(spell, "");
    }

    public static ITextComponent getSpellInfoText(Spell spell, String translationKey)
    {
        TranslationTextComponent spellText = new TranslationTextComponent(spell.getTranslationKey());

        TextFormatting col = TextFormatting.fromColorIndex(StringHelper.dyeTextMap.get(spell.getColor().getId()));
        if (col != null) {
            spellText.setStyle((new Style()).setColor(col));
        }

        //spellText.setStyle((new Style()).setColor(spell.getColor().getFormatColor()));
        if (translationKey.isEmpty()) {
            return spellText;
        }
        return new TranslationTextComponent(translationKey, spellText);
    }

    public static void addSpellDescription(Spell spell, List<ITextComponent> tooltip)
    {
        int cost = spell.getApplyCost();

        // add description
        ITextComponent descriptionText = new TranslationTextComponent(spell.getDescriptionKey());
        descriptionText.setStyle((new Style()).setColor(TextFormatting.WHITE));
        tooltip.add(descriptionText);

        // add text to show what the spell affects
        ITextComponent affectText = new TranslationTextComponent(spell.getAffectKey());
        affectText.setStyle((new Style()).setColor(TextFormatting.GRAY));
        tooltip.add(affectText);

        // add cost if needed
        if (cost > 0) {
            String key = cost == 1 ? "spell.strange.level_cost" : "spell.strange.levels_cost";
            ITextComponent applyCostText = new TranslationTextComponent(key, spell.getApplyCost());
            applyCostText.setStyle((new Style()).setColor(TextFormatting.GREEN));
            tooltip.add(applyCostText);
        }

        // add activation message if needed
        if (spell.needsActivation()) {
            ITextComponent activateText = new TranslationTextComponent("spell.strange.needs_activation");
            activateText.setStyle((new Style()).setColor(TextFormatting.RED));
            tooltip.add(activateText);
        }
    }

    @Nullable
    public static Spell getRandomSpell(Random rand)
    {
        String spellId = Spells.enabledSpells.get(rand.nextInt(Spells.enabledSpells.size()));
        if (spellId == null || spellId.isEmpty()) return null;
        if (!Spells.spells.containsKey(spellId)) return null;
        return Spells.spells.get(spellId);
    }

    public static boolean checkEnoughXp(PlayerEntity player, int cost)
    {
        if (!player.isCreative() && player.experienceLevel < cost) {
            player.sendStatusMessage(SpellsHelper.getSpellInfoText(spell, "event.strange.spellbook.not_enough_xp"), true);
            return false;
        }

        return true;
    }
}
