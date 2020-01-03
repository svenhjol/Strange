package svenhjol.strange.spells.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import svenhjol.strange.Strange;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.particles.MagicEnchantParticle;
import svenhjol.strange.spells.particles.MagicSpellParticle;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Strange.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpellsClient
{
    @SubscribeEvent
    public static void onParticleRegister(ParticleFactoryRegisterEvent event)
    {
        Minecraft mc = Minecraft.getInstance();

        mc.particles.registerFactory(Spells.spellParticle, MagicSpellParticle.MagicSpellFactory::new);
        mc.particles.registerFactory(Spells.enchantParticle, MagicEnchantParticle.MagicEnchantFactory::new);

        // really lame.  TODO is there a better way to do this?



//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.WHITE), MagicSpellParticle.WhiteFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.ORANGE), MagicSpellParticle.OrangeFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.MAGENTA), MagicSpellParticle.MagentaFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.LIGHT_BLUE), MagicSpellParticle.LightBlueFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.YELLOW), MagicSpellParticle.YellowFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.LIME), MagicSpellParticle.LimeFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.PINK), MagicSpellParticle.PinkFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.GRAY), MagicSpellParticle.GrayFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.LIGHT_GRAY), MagicSpellParticle.LightGrayFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.CYAN), MagicSpellParticle.CyanFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.PURPLE), MagicSpellParticle.PurpleFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.BLUE), MagicSpellParticle.BlueFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.BROWN), MagicSpellParticle.BrownFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.GREEN), MagicSpellParticle.GreenFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.RED), MagicSpellParticle.RedFactory::new);
//        mc.particles.registerFactory(Spells.spellParticles.get(DyeColor.BLACK), MagicSpellParticle.BlackFactory::new);
//
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.WHITE), MagicEnchantParticle.WhiteFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.ORANGE), MagicEnchantParticle.OrangeFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.MAGENTA), MagicEnchantParticle.MagentaFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.LIGHT_BLUE), MagicEnchantParticle.LightBlueFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.YELLOW), MagicEnchantParticle.YellowFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.LIME), MagicEnchantParticle.LimeFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.PINK), MagicEnchantParticle.PinkFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.GRAY), MagicEnchantParticle.GrayFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.LIGHT_GRAY), MagicEnchantParticle.LightGrayFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.CYAN), MagicEnchantParticle.CyanFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.PURPLE), MagicEnchantParticle.PurpleFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.BLUE), MagicEnchantParticle.BlueFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.BROWN), MagicEnchantParticle.BrownFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.GREEN), MagicEnchantParticle.GreenFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.RED), MagicEnchantParticle.RedFactory::new);
//        mc.particles.registerFactory(Spells.enchantParticles.get(DyeColor.BLACK), MagicEnchantParticle.BlackFactory::new);
    }
}
