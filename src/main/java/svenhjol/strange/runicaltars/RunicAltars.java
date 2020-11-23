package svenhjol.strange.runicaltars;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

@Module(mod = Strange.MOD_ID, client = RunicAltarsClient.class, description = "Craftable tablets that can teleport you to a point of interest or the location of a lodestone.")
public class RunicAltars extends CharmModule {
    public static final Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "runic_altar");

    public static RunicAltarBlock RUNIC_ALTAR;
    public static BlockEntityType<RunicAltarBlockEntity> BLOCK_ENTITY;
    public static ScreenHandlerType<RunicAltarScreenHandler> SCREEN_HANDLER;

    @Override
    public void register() {
        RUNIC_ALTAR = new RunicAltarBlock(this);
        SCREEN_HANDLER = RegistryHandler.screenHandler(BLOCK_ID, RunicAltarScreenHandler::new);
        BLOCK_ENTITY = RegistryHandler.blockEntity(BLOCK_ID, RunicAltarBlockEntity::new);
    }
}
