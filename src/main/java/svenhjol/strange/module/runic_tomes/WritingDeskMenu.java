package svenhjol.strange.module.runic_tomes;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class WritingDeskMenu extends AbstractContainerMenu {
    public WritingDeskMenu(int syncId, Inventory inventory) {
        this(syncId, inventory, ContainerLevelAccess.NULL);
    }

    public WritingDeskMenu(int syncId, Inventory inventory, ContainerLevelAccess access) {
        super(RunicTomes.WRITING_DESK_MENU, syncId);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
