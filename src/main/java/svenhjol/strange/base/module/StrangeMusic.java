package svenhjol.strange.base.module;

import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.item.Rarity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
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

@Module(mod = Strange.MOD_ID, category = StrangeCategories.BASE, hasSubscriptions = true)
public class StrangeMusic extends MesonModule
{
    private static final String NAME = "svenhjol";
    private static CharmMusicDiscItem strangemusic;

    @Config(name = "Play Strange music", description = "If true, allows for custom music tracks to play in certain situations.\n" +
        "Charm's 'Ambient Music Improvements' module must be enabled for this to work.")
    public static boolean music = true;

    @Override
    public void init()
    {
        Item.Properties props = new Item.Properties().maxStackSize(1).rarity(Rarity.RARE);
        strangemusic = new CharmMusicDiscItem(this, "music_disc_strange", StrangeSounds.MUSIC_DISC, props,0);
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        if (!music) return;
        if (!Charm.hasModule(AmbientMusicImprovements.class)) return;

        // play Þarna in overworld anywhere
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_THARNA, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null || player.world == null) return false;
            return player.world.rand.nextFloat() < 0.1F
                && player.world.getDimension().getType() == DimensionType.OVERWORLD;
        }));

        // play Steinn in overworld underground
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_STEINN, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null || player.world == null) return false;
            return player.getPosition().getY() < 48
                && player.world.getDimension().getType() == DimensionType.OVERWORLD
                && player.world.rand.nextFloat() < 0.2F;
        }));

        // play Mús in cold environments
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_MUS, 1200, 3600, mc ->
            mc.player != null
            && mc.player.world.getBiome(new BlockPos(mc.player)).getCategory() == Biome.Category.ICY
            && mc.player.world.rand.nextFloat() < 0.33F
        ));

        // play Undir in nether underground
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_UNDIR, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null) return false;
            return player.getPosition().getY() < 48
                && player.world.getDimension().getType() == DimensionType.THE_NETHER
                && player.world.rand.nextFloat() < 0.33F;
        }));
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
}
