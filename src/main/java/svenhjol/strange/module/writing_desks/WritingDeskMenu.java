package svenhjol.strange.module.writing_desks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.UUID;

public class WritingDeskMenu extends AbstractContainerMenu {
    public static final int DELETE = -1;

    private final Inventory playerInventory;
    private final Player player;
    private final ContainerLevelAccess access;
    private final Container inputSlots = new SimpleContainer(2) {

    };
    private final ResultContainer resultSlots = new ResultContainer();

    public WritingDeskMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public WritingDeskMenu(int syncId, Inventory playerInventory, ContainerLevelAccess access) {
        super(WritingDesks.MENU, syncId);

        this.access = access;
        this.player = playerInventory.player;
        this.playerInventory = playerInventory;

        // book slot
        this.addSlot(new Slot(inputSlots, 0, 9, 25) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (!WritingDeskMenu.this.player.level.isClientSide) {
                    WritingDesks.writtenRunes.remove(WritingDeskMenu.this.player.getUUID());
                }
                slotsChanged(inputSlots);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.BOOK;
            }
        });

        // ink slot
        this.addSlot(new Slot(inputSlots, 1, 9, 47) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(inputSlots);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.INK_SAC;
            }
        });

        this.addSlot(new Slot(resultSlots, 2, 150, 35) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true;
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                WritingDeskMenu.this.onTake(player, itemStack);
            }
        });

        // TODO: abstract this
        int k;
        for(k = 0; k < 3; ++k) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 128 + k * 18));
            }
        }

        for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 186));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) -> {
            if (!this.isValidBlock(level.getBlockState(pos))) {
                return false;
            }
            return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.inputSlots);
        });
    }

    private void onTake(Player player, ItemStack stack) {
    }

    private void createResult() {
        resultSlots.setItem(0, new ItemStack(Items.BLAZE_ROD)); // TODO: silly testdata
    }

    private void clearResult() {
        resultSlots.setItem(0, ItemStack.EMPTY);
    }

    private boolean isValidBlock(BlockState state) {
        return state.getBlock() == WritingDesks.WRITING_DESK;
    }

    @Override
    public boolean clickMenuButton(Player player, int r) {
        if (player.level.isClientSide) {
            return false;
        }

        ServerPlayer serverPlayer = (ServerPlayer)player;
        UUID uuid = serverPlayer.getUUID();
        String runes = WritingDesks.writtenRunes.computeIfAbsent(uuid, s -> "");

        if (r == DELETE) {
            runes = runes.substring(0, runes.length() - 1);
        } else if (runes.length() < KnowledgeData.MAX_LENGTH) {
            runes += String.valueOf((char)(r + 97));
        }

        boolean hasValidRunes = KnowledgeHelper.isValidRuneString(runes);
        WritingDesks.writtenRunes.put(uuid, runes);

        if (hasValidRunes) {
            createResult();
        } else {
            clearResult();
        }

        NetworkHelper.sendPacketToClient(serverPlayer, WritingDesks.MSG_CLIENT_VALID_RUNES, buf -> buf.writeBoolean(hasValidRunes));

        return true;
    }

    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            if (index < this.inputSlots.getContainerSize()) {
                if (!this.moveItemStackTo(stackInSlot, this.inputSlots.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, this.inputSlots.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stack;
    }
}
