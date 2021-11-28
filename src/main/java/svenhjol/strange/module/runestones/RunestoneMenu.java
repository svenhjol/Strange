package svenhjol.strange.module.runestones;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.menu.CharmContainerMenu;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;
import svenhjol.strange.module.runestones.event.ActivateRunestoneCallback;

public class RunestoneMenu extends CharmContainerMenu {
    private final Inventory playerInventory;
    private final Player player;
    private final Container inventory;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public RunestoneMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(1), new SimpleContainerData(1), ContainerLevelAccess.NULL);
    }

    public RunestoneMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data, ContainerLevelAccess access) {
        super(Runestones.MENU, syncId, playerInventory, inventory);

        this.inventory = inventory;
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;
        this.data = data;
        this.access = access;
        this.addDataSlots(data);

        this.addSlot(new Slot(inventory, 0, 80, 55) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(inventory);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        // TODO: abstract this
        int k;
        for(k = 0; k < 3; ++k) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
            }
        }

        for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int i) {
        switch (i) {
            case 1 -> access.execute((level, pos) -> {
                if (level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone) {
                    String runes = runestone.getRunes();
                    ItemStack sacrifice = slots.get(0).getItem();
                    ActivateRunestoneCallback.EVENT.invoker().interact((ServerPlayer) player, pos, runes, sacrifice);

                    if (!sacrifice.isEmpty()) {
                        sacrifice.shrink(1);
                    }
                }
                broadcastChanges();
            });
        }
        return true;
    }

    public IRunestoneMaterial getMaterial() {
        return RunestoneMaterial.getById(this.data.get(0));
    }
}
