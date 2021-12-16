package svenhjol.strange.module.runic_tomes;

import net.minecraft.server.level.ServerPlayer;
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
import svenhjol.charm.menu.CharmContainerMenu;
import svenhjol.strange.module.runic_tomes.event.ActivateRunicTomeCallback;

public class RunicLecternMenu extends CharmContainerMenu {
    private final Inventory playerInventory;
    private final Player player;
    private final Container inventory;
    private final ContainerLevelAccess access;

    public RunicLecternMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(1), ContainerLevelAccess.NULL);
    }

    public RunicLecternMenu(int syncId, Inventory playerInventory, Container inventory, ContainerLevelAccess access) {
        super(RunicTomes.RUNIC_LECTERN_MENU, syncId, playerInventory, inventory);

        this.access = access;
        this.inventory = inventory;
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;

        // sacrificial item
        this.addSlot(new Slot(inventory, 0, 80, 62) {
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
        switch (i) {
            case 0 -> access.execute((level, pos) -> {
                ItemStack sacrifice = slots.get(0).getItem();
                Inventory inventory = player.getInventory();

                if (!sacrifice.isEmpty()) {
                    inventory.placeItemBackInInventory(sacrifice);
                }

                if (level.getBlockEntity(pos) instanceof RunicLecternBlockEntity lectern) {
                    ItemStack tome = lectern.getTome();

                    inventory.placeItemBackInInventory(tome);

                    BlockState state = level.getBlockState(pos);
                    BlockState newState = Blocks.LECTERN.defaultBlockState();
                    newState = newState.setValue(LecternBlock.FACING, state.getValue(RunicLecternBlock.FACING));
                    level.removeBlockEntity(pos);
                    level.setBlock(pos, newState, 2);
                }

            });
            case 1 -> access.execute((level, pos) -> {
                if (level.getBlockEntity(pos) instanceof RunicLecternBlockEntity lectern) {
                    ItemStack tome = lectern.getTome();
                    ItemStack sacrifice = slots.get(0).getItem();
                    ActivateRunicTomeCallback.EVENT.invoker().interact((ServerPlayer) player, pos, tome, sacrifice);

                    if (!sacrifice.isEmpty()) {
                        sacrifice.shrink(1);
                    }

                    // Update the lectern's time of activation and the state of the item it holds.
                    lectern.setActivatedTicks(level.getGameTime());
                    lectern.setItem(0, sacrifice);
                    lectern.setChanged();
                }

                broadcastChanges();
            });
        }
        return true;
    }
}
