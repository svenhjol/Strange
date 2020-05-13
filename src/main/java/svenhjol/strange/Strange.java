package svenhjol.strange;

import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import svenhjol.meson.MesonInstance;
import svenhjol.meson.handler.LogHandler;
import svenhjol.strange.base.*;
import svenhjol.strange.base.feature.StrangeJigsawPiece;

@Mod(Strange.MOD_ID)
public class Strange extends MesonInstance {
    public static final String MOD_ID = "strange";
    public static final LogHandler LOG = new LogHandler(Strange.MOD_ID);
    public static StrangeClient client;
    public static StrangeServer server;

    public static final Marker CLIENT = MarkerManager.getMarker("CLIENT");

    public Strange() {
        super(Strange.MOD_ID, LOG);

        StrangeSounds.init(this);
        StrangeLoot.init(this);
        StrangeMessages.init(this);

        StrangeJigsawPiece.STRANGE_POOL_ELEMENT = IJigsawDeserializer.register("strange_pool_element", StrangeJigsawPiece::new);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        super.onCommonSetup(event);
        Strange.server = new StrangeServer();
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        super.onClientSetup(event);
        Strange.client = new StrangeClient();
    }
}
