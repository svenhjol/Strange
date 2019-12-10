package svenhjol.strange.spells.helper;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;

import javax.annotation.Nullable;
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

    @Nullable
    public static Spell getRandomSpell(Random rand, boolean rare)
    {
        String spellId;

        if (!rare) {
            spellId = Spells.commonSpells.get(rand.nextInt(Spells.commonSpells.size()));
        } else {
            spellId = Spells.enabledSpells.get(rand.nextInt(Spells.enabledSpells.size()));
        }
        if (spellId == null || spellId.isEmpty()) return null;
        if (!Spells.spells.containsKey(spellId)) return null;
        return Spells.spells.get(spellId);
    }
}
