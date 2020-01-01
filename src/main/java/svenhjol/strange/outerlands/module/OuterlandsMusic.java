package svenhjol.strange.outerlands.module;

import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.item.Rarity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.charm.Charm;
import svenhjol.charm.tweaks.client.AmbientMusicClient;
import svenhjol.charm.tweaks.item.CharmMusicDiscItem;
import svenhjol.charm.tweaks.module.AmbientMusicImprovements;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.OUTERLANDS, hasSubscriptions = true)
public class OuterlandsMusic extends MesonModule
{
    private static final String NAME = "svenhjol";

    private static CharmMusicDiscItem outerlands;

    @Config(name = "Play outerlands music", description = "If true, extra music may play when the player is in the Outerlands.\n" +
        "Charm's 'Ambient Music Improvements' module must be enabled for this to work.")
    public static boolean music = true;

    @Override
    public void init()
    {
        Item.Properties props = new Item.Properties().maxStackSize(1).rarity(Rarity.RARE);
        outerlands = new CharmMusicDiscItem(this, "music_disc_outerlands", StrangeSounds.MUSIC_DISC, props,0);
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        if (!music) return;
        if (!Charm.hasModule(AmbientMusicImprovements.class)) return;

        // play Þarna in outerlands anywhere
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_THARNA, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null) return false;
            return Outerlands.isOuterPos(player.getPosition()) && player.world.rand.nextFloat() < 0.25F;
        }));

        // play Steinn in outerlands underground
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_STEINN, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null) return false;
            return Outerlands.isOuterPos(player.getPosition())
                && player.getPosition().getY() < 48
                && player.world.rand.nextFloat() < 0.25F;
        }));

        // play Mús in outerlands cold environments
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_MUS, 1200, 3600, mc ->
            mc.player != null
                && (!mc.player.isCreative() || !mc.player.isSpectator())
                && Outerlands.isOuterPos(mc.player.getPosition())
                && mc.player.world.getBiome(new BlockPos(mc.player)).getCategory() == Biome.Category.ICY
                && mc.player.world.rand.nextFloat() < 0.33F
        ));
    }

    @SubscribeEvent
    public void onName(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getTarget() instanceof FoxEntity
            && music
            && event.getPlayer() != null
            && ((FoxEntity)event.getTarget()).getVariantType() == FoxEntity.Type.SNOW
        ) {
            FoxEntity fox = (FoxEntity)event.getTarget();
            ItemStack held = event.getPlayer().getHeldItem(event.getHand());
            if (!(held.getItem() instanceof NameTagItem)) return;
            if (fox.getDisplayName().getUnformattedComponentText().equals(NAME)) return;

            String name = held.getDisplayName().getUnformattedComponentText();
            if (name.equals(NAME)) {
                fox.setHeldItem(Hand.MAIN_HAND, new ItemStack(outerlands));
            }
        }
    }
}
