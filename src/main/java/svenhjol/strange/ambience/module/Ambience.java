package svenhjol.strange.ambience.module;

import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.item.Rarity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.charm.tweaks.item.CharmMusicDiscItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.ambience.client.AmbienceClient;
import svenhjol.strange.ambience.client.MusicClient;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.AMBIENCE, hasSubscriptions = true)
public class Ambience extends MesonModule
{
    private static final String NAME = "svenhjol";
    private static CharmMusicDiscItem strangemusic;

    @Config(name = "Play music", description = "If true, custom music tracks will play in certain situations.\n" +
        "Charm's 'Ambient Music Improvements' module must be enabled for this to work.")
    public static boolean music = true;

    @Config(name = "Play ambient sounds", description = "If true, background ambient sounds play in biomes, caves and other dimensions.")
    public static boolean ambience = true;

    @OnlyIn(Dist.CLIENT)
    public static MusicClient musicClient;

    @OnlyIn(Dist.CLIENT)
    public static AmbienceClient ambienceClient;

    @Override
    public void init()
    {
        Item.Properties props = new Item.Properties().maxStackSize(1).rarity(Rarity.RARE);
        strangemusic = new CharmMusicDiscItem(this, "music_disc_strange", StrangeSounds.MUSIC_DISC, props,0);
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event)
    {
        musicClient = new MusicClient();
        ambienceClient = new AmbienceClient();
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
                fox.setHeldItem(Hand.MAIN_HAND, new ItemStack(strangemusic));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity
            && event.getEntity().world.isRemote
        ) {
            ambienceClient.playerJoined((PlayerEntity)event.getEntity());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END
            && event.player.world.isRemote
            && ambienceClient.handler != null
        ) {
            ambienceClient.handler.tick();
        }
    }
}
