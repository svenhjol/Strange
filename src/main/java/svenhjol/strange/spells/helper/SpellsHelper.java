package svenhjol.strange.spells.helper;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import svenhjol.strange.spells.spells.Spell;

public class SpellsHelper
{
    public static ITextComponent getSpellInfoText(Spell spell)
    {
        return getSpellInfoText(spell, "");
    }

    public static ITextComponent getSpellInfoText(Spell spell, String translationKey)
    {
        TranslationTextComponent spellText = new TranslationTextComponent(spell.getTranslationKey());
        spellText.setStyle((new Style()).setColor(spell.getElement().getFormatColor()));
        if (translationKey.isEmpty()) {
            return spellText;
        }
        return new TranslationTextComponent(translationKey, spellText);
    }

    public static ITextComponent getSpellElementText(Spell spell)
    {
        TranslationTextComponent elementText = new TranslationTextComponent("spell.strange.element." + spell.getElement().getName());
        elementText.setStyle((new Style()).setColor(spell.getElement().getFormatColor()));
        return elementText;

    }
}
