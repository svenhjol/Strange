package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID)
public class RunicTomes extends CharmModule {
    public static final ResourceLocation RUNIC_LECTERN_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runic_lectern");
    public static final ResourceLocation MSG_CLIENT_SET_LECTERN_TOME = new ResourceLocation(Strange.MOD_ID, "client_set_lectern_tome");

    public static RunicLecternBlock RUNIC_LECTERN;
    public static MenuType<RunicLecternMenu> RUNIC_LECTERN_MENU;
    public static BlockEntityType<RunicLecternBlockEntity> RUNIC_LECTERN_BLOCK_ENTITY;
    public static RunicTomeItem RUNIC_TOME;

    @Override
    public void register() {
        RUNIC_TOME = new RunicTomeItem(this);
        RUNIC_LECTERN = new RunicLecternBlock(this);
        RUNIC_LECTERN_MENU = CommonRegistry.menu(RUNIC_LECTERN_BLOCK_ID, RunicLecternMenu::new);
        RUNIC_LECTERN_BLOCK_ENTITY = CommonRegistry.blockEntity(RUNIC_LECTERN_BLOCK_ID, RunicLecternBlockEntity::new, RUNIC_LECTERN);
    }

    @Override
    public void runWhenEnabled() {
        UseBlockCallback.EVENT.register(this::handleUseBlock);
    }

    private InteractionResult handleUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockPos hitPos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(hitPos);
            Block block = state.getBlock();

            if (block instanceof LecternBlock
                && level.getBlockEntity(hitPos) instanceof LecternBlockEntity lectern
                && !lectern.hasBook()
                && player.getItemInHand(hand).getItem() instanceof RunicTomeItem
            ) {
                Direction facing = state.getValue(LecternBlock.FACING);
                BlockState newState = RUNIC_LECTERN.defaultBlockState();
                newState = newState.setValue(RunicLecternBlock.FACING, facing);
                level.setBlock(hitPos, newState, 2);
                if (level.getBlockEntity(hitPos) instanceof RunicLecternBlockEntity runicLectern) {
                    ItemStack held = player.getItemInHand(hand);
                    ItemStack tome = held.copy();
                    held.shrink(1);
                    runicLectern.setTome(tome);

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
