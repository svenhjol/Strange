package svenhjol.strange.magic.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import svenhjol.strange.Strange;
import svenhjol.strange.magic.module.Magic;
import svenhjol.strange.magic.particles.MagicEnchantParticle;
import svenhjol.strange.magic.particles.MagicSpellParticle;
import svenhjol.strange.magic.spells.Spell.Element;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Strange.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpellsClient
{
    @SubscribeEvent
    public static void onParticleRegister(ParticleFactoryRegisterEvent event)
    {
        Minecraft mc = Minecraft.getInstance();

        // TOOO there must be a way of doing this programmatically
        mc.particles.registerFactory(Magic.spellParticles.get(Element.BASE), MagicSpellParticle.MagicSpellFactory::new);
        mc.particles.registerFactory(Magic.spellParticles.get(Element.AIR), MagicSpellParticle.AirSpellFactory::new);
        mc.particles.registerFactory(Magic.spellParticles.get(Element.WATER), MagicSpellParticle.WaterSpellFactory::new);
        mc.particles.registerFactory(Magic.spellParticles.get(Element.EARTH), MagicSpellParticle.EarthSpellFactory::new);
        mc.particles.registerFactory(Magic.spellParticles.get(Element.FIRE), MagicSpellParticle.FireSpellFactory::new);

        mc.particles.registerFactory(Magic.enchantParticles.get(Element.BASE), MagicEnchantParticle.MagicEnchantFactory::new);
        mc.particles.registerFactory(Magic.enchantParticles.get(Element.AIR), MagicEnchantParticle.AirEnchantFactory::new);
        mc.particles.registerFactory(Magic.enchantParticles.get(Element.WATER), MagicEnchantParticle.WaterEnchantFactory::new);
        mc.particles.registerFactory(Magic.enchantParticles.get(Element.EARTH), MagicEnchantParticle.EarthEnchantFactory::new);
        mc.particles.registerFactory(Magic.enchantParticles.get(Element.FIRE), MagicEnchantParticle.FireEnchantFactory::new);
    }
}
