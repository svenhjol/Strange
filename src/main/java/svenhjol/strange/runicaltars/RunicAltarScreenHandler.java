package svenhjol.strange.runicaltars;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import svenhjol.charm.base.screenhandler.CharmScreenHandler;

public class RunicAltarScreenHandler extends CharmScreenHandler {
    private final PlayerInventory playerInventory;
    private final PlayerEntity player;
    private final Inventory inventory;
    private final ScreenHandlerContext context;

    public RunicAltarScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(RunicAltarBlockEntity.SIZE), ScreenHandlerContext.EMPTY);
    }

    public RunicAltarScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, ScreenHandlerContext context) {
        super(RunicAltars.SCREEN_HANDLER, syncId, playerInventory, inventory);

        this.inventory = inventory;
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;
        this.context = context;

        this.addSlot(new Slot(inventory, 0, 80, 34) {
            @Override
            public void markDirty() {
                super.markDirty();
                onContentChanged(inventory);
            }

            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == Items.COMPASS;
            }
        });

        // this adds the player inventory
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
}
