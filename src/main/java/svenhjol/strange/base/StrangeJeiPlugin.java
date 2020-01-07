package svenhjol.strange.base;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.util.ResourceLocation;
import svenhjol.strange.Strange;
import svenhjol.strange.spells.module.SpellBooks;
import svenhjol.strange.scrolls.module.Scrolls;

@JeiPlugin
public class StrangeJeiPlugin implements IModPlugin
{
    private static final ResourceLocation UID = new ResourceLocation(Strange.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid()
    {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration)
    {
        registration.useNbtForSubtypes(SpellBooks.book);
        registration.useNbtForSubtypes(Scrolls.item);
    }
}
