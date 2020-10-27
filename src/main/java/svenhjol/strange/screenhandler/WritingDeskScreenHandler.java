package svenhjol.strange.screenhandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import svenhjol.strange.module.RunicTablets;
import svenhjol.strange.module.WritingDesks;

public class WritingDeskScreenHandler extends ScreenHandler {
    private final Inventory input;
    private final Inventory result;
    private final ScreenHandlerContext context;

    public WritingDeskScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public WritingDeskScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(WritingDesks.SCREEN_HANDLER, syncId);

        this.result = new CraftingResultInventory();
        this.input = new SimpleInventory(4) {
            public void markDirty() {
                super.markDirty();
                WritingDeskScreenHandler.this.onContentChanged(this);
            }
        };
        this.context = context;

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                this.addSlot(new Slot(this.input, 0, 49 + (x * 2), 19 + (y * 2)) {
                    public boolean canInsert(ItemStack stack) {
                        return stack.getItem() == RunicTablets.RUNIC_TABLET;
                    }
                });
            }
        }
        this.addSlot(new Slot(this.result, 2, 129, 34) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
                context.run((world, pos) -> {

                });
                for (int i = 0; i < 4; i++) {
                    WritingDeskScreenHandler.this.input.setStack(i, ItemStack.EMPTY);
                }
                return stack;
            }
        });
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, WritingDesks.WRITING_DESK);
    }

    /**
     * Copypasta from GrindstoneScreenHandler.
     * Probably needs making abstract, but not CharmScreenHandler.
     */
    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            ItemStack itemStack3 = this.input.getStack(0);
            ItemStack itemStack4 = this.input.getStack(1);
            if (index == 2) {
                if (!this.insertItem(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onStackChanged(itemStack2, itemStack);
            } else if (index != 0 && index != 1) {
                if (!itemStack3.isEmpty() && !itemStack4.isEmpty()) {
                    if (index >= 3 && index < 30) {
                        if (!this.insertItem(itemStack2, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= 30 && index < 39 && !this.insertItem(itemStack2, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(itemStack2, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, world, this.input);
        });
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    private void updateResult() {
        this.sendContentUpdates();
    }
}
