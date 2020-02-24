package svenhjol.strange.spells.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.strange.spells.tile.SpellLecternTileEntity;

@OnlyIn(Dist.CLIENT)
public class SpellLecternsClient
{
    public void onClientSetup(FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntitySpecialRenderer(SpellLecternTileEntity.class, new SpellLecternTileEntityRenderer());
    }
}
