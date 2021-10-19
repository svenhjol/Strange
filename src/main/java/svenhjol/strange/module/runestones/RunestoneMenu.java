package svenhjol.strange.module.runestones;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.screen.CharmContainerMenu;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;

public class RunestoneMenu extends CharmContainerMenu {
    private final Inventory playerInventory;
    private final Player player;
    private final Container inventory;
    private final ContainerData data;

    public RunestoneMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(1), new SimpleContainerData(1));
    }

    public RunestoneMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data) {
        super(Runestones.MENU, syncId, playerInventory, inventory);

        this.inventory = inventory;
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;
        this.data = data;
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

    public IRunestoneMaterial getMaterial() {
        return RunestoneMaterial.getById(this.data.get(0));
    }
}
