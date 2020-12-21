package svenhjol.strange.runicaltars;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

@Module(mod = Strange.MOD_ID, client = RunicAltarsClient.class, description = "Craftable tablets that can teleport you to a point of interest or the location of a lodestone.")
public class RunicAltars extends CharmModule {
    public static final int NUMBER_OF_RUNES = 8;
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

    @Override
    public void init() {

        // DispenserBehavior#registerDefaults:525
        DispenserBlock.registerBehavior(Items.CHORUS_FLOWER, new FallibleItemDispenserBehavior() {
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
                BlockPos blockPos = pointer.getBlockPos().offset(direction);
                World world = pointer.getWorld();
                BlockState blockState = world.getBlockState(blockPos);
                this.setSuccess(true);
                if (blockState.isOf(RUNIC_ALTAR)) {
                    if (blockState.get(RunicAltarBlock.CHARGES) != 4) {
                        RunicAltarBlock.charge(world, blockPos, blockState);
                        stack.decrement(1);
                    } else {
                        this.setSuccess(false);
                    }

                    return stack;
                } else {
                    return super.dispenseSilently(pointer, stack);
                }
            }
        });
    }
}
