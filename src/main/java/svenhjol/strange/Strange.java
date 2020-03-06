package svenhjol.strange;

import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.meson.MesonInstance;
import svenhjol.meson.handler.LogHandler;
import svenhjol.strange.base.*;
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

        StrangeSounds.init(this);
        StrangeLoot.init(this);
        StrangeMessages.init(this);
        StrangeCompat.init(this);

        StrangeJigsawPiece.STRANGE_POOL_ELEMENT = IJigsawDeserializer.register("strange_pool_element", StrangeJigsawPiece::new);
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event)
    {
        super.onClientSetup(event);
        Strange.client = new StrangeClient();
    }
}
