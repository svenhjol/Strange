package svenhjol.strange.runicaltars;

import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

@Module(mod = Strange.MOD_ID, client = RunicAltarsClient.class, description = "Craftable tablets that can teleport you to a point of interest or the location of a lodestone.")
public class RunicAltars extends CharmModule {
    public static final int NUMBER_OF_RUNES = 8;
    public static final Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "runic_altar");
    public static final Identifier MSG_SERVER_PLACED_ON_ALTAR = new Identifier(Strange.MOD_ID, "server_placed_on_altar");

    public static RunicAltarBlock RUNIC_ALTAR;
    public static BlockEntityType<RunicAltarBlockEntity> BLOCK_ENTITY;
    public static ScreenHandlerType<RunicAltarScreenHandler> SCREEN_HANDLER;

    @Override
    public void register() {
        RUNIC_ALTAR = new RunicAltarBlock(this);
        SCREEN_HANDLER = RegistryHandler.screenHandler(BLOCK_ID, RunicAltarScreenHandler::new);
        BLOCK_ENTITY = RegistryHandler.blockEntity(BLOCK_ID, RunicAltarBlockEntity::new);
    }

    @Override
    public void init() {
        ServerSidePacketRegistry.INSTANCE.register(RunicAltars.MSG_SERVER_PLACED_ON_ALTAR, this::handleServerPlacedOnAltar);
    }

    private void handleServerPlacedOnAltar(PacketContext context, PacketByteBuf data) {
        BlockPos pos = BlockPos.fromLong(data.readLong());
        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            if (player == null) return;

            World world = player.world;
            RunicAltarBlockEntity blockEntity = (RunicAltarBlockEntity)world.getBlockEntity(pos);
            if (blockEntity == null)
                return;

            ItemStack stack = blockEntity.getStack(0);
            Criteria.CONSUME_ITEM.trigger(player, stack);
        });
    }
}
