package svenhjol.strange;

import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.meson.MesonInstance;
import svenhjol.meson.handler.LogHandler;
import svenhjol.meson.helper.ForgeHelper;
import svenhjol.strange.base.StrangeClient;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.base.StrangeMessages;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.base.compat.QuarkCompat;
import svenhjol.strange.base.feature.StrangeJigsawPiece;

@Mod(Strange.MOD_ID)
public class Strange extends MesonInstance
{
    public static final String MOD_ID = "strange";
    public static LogHandler LOG = new LogHandler(Strange.MOD_ID);
    public static QuarkCompat quarkCompat;
    public static StrangeClient client;

    public Strange()
    {
        super(Strange.MOD_ID, LOG);

        StrangeSounds.init();
        StrangeLoot.init();
        StrangeMessages.init();

        // compat
        try
        {
            if (ForgeHelper.isModLoaded("quark"))
                quarkCompat = QuarkCompat.class.newInstance();
        }
        catch (Exception e)
        {
            LOG.error("Error loading Quark compat");
        }

        StrangeJigsawPiece.STRANGE_POOL_ELEMENT = IJigsawDeserializer.register("strange_pool_element", StrangeJigsawPiece::new);
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event)
    {
        super.onClientSetup(event);
        Strange.client = new StrangeClient();
    }
}
