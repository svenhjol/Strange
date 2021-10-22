package svenhjol.strange.module.writing_desks;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.screen.CharmContainerMenu;

public class RunicLecternMenu extends CharmContainerMenu {
    private final Inventory playerInventory;
    private final Player player;
    private final Container inventory;
    private final ContainerLevelAccess access;

    public RunicLecternMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(1), ContainerLevelAccess.NULL);
    }

    public RunicLecternMenu(int syncId, Inventory playerInventory, Container inventory, ContainerLevelAccess access) {
        super(WritingDesks.RUNIC_LECTERN_MENU, syncId, playerInventory, inventory);

        this.access = access;
        this.inventory = inventory;
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;

        // sacrificial item
        this.addSlot(new Slot(inventory, 0, 80, 61) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(inventory);
            }

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }
        });

        // TODO: abstract this
        int k;
        for(k = 0; k < 3; ++k) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 92 + k * 18));
            }
        }

        for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 150));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int i) {
//        if (player.level.isClientSide) {
//            return false;
//        }

        switch (i) {
            case 0 -> access.execute((level, pos) -> {
                ItemStack sacrifice = slots.get(0).getItem();
                if (!sacrifice.isEmpty()) {
                    PlayerHelper.addOrDropStack(player, sacrifice);
                }

                if (level.getBlockEntity(pos) instanceof RunicLecternBlockEntity lectern) {
                    ItemStack tome = lectern.getTome();

                    Inventory inventory = player.getInventory();
                    inventory.placeItemBackInInventory(tome);
//                    PlayerHelper.addOrDropStack(player, tome);

                    BlockState state = level.getBlockState(pos);
                    BlockState newState = Blocks.LECTERN.defaultBlockState();
                    newState = newState.setValue(LecternBlock.FACING, state.getValue(RunicLecternBlock.FACING));
                    level.setBlock(pos, newState, 2);
                }

            });
            case 1 -> access.execute((level, pos) -> {
                if (level.getBlockEntity(pos) instanceof RunicLecternBlockEntity lectern) {
                    ItemStack tome = lectern.getTome();

                    // TODO: teleport calculation from tome

                    ItemStack sacrifice = slots.get(0).getItem();
                    if (!sacrifice.isEmpty()) {
                        sacrifice.shrink(1);
                    }
                }

                broadcastChanges();
            });
        }
        return true;
    }
}
