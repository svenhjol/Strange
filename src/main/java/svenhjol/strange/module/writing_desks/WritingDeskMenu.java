package svenhjol.strange.module.writing_desks;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
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
        boolean isCreative = player.getAbilities().instabuild;

        if (!isCreative) {
            ItemStack books = this.inputSlots.getItem(0);
            ItemStack ink = this.inputSlots.getItem(1);

            books.shrink(1);
            ink.shrink(1);

            this.inputSlots.setItem(0, books);
            this.inputSlots.setItem(1, ink);
        }

        WritingDesks.writtenRunes.remove(player.getUUID());

        this.access.execute((level, pos) -> {
            level.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
        });
    }

    private void createResult(ServerPlayer player, String runes) {
        ItemStack tome = new ItemStack(WritingDesks.RUNIC_TOME);
        RunicTomeItem.setRunes(tome, runes);
        RunicTomeItem.setAuthor(tome, player.getName().getString());

        // get the name of the location according to the branch
        KnowledgeBranch.getByStartRune(runes.charAt(0)).flatMap(branch
            -> branch.getPrettyName(runes)).ifPresent(name
                -> tome.setHoverName(new TextComponent(name)));

        if (!tome.hasCustomHoverName()) {
            tome.setHoverName(new TranslatableComponent("gui.strange.writing_desks.runic_tome"));
        }

        resultSlots.setItem(0, tome);
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

        // don't allow writing if there is no book or ink
        if (inputSlots.getItem(0).isEmpty() || inputSlots.getItem(1).isEmpty()) {
            WritingDesks.writtenRunes.remove(uuid);
            return false;
        }

        if (r == DELETE) {
            runes = runes.substring(0, runes.length() - 1);
        } else if (runes.length() < Knowledge.MAX_LENGTH) {
            runes += String.valueOf((char)(r + Knowledge.ALPHABET_START));
        }

        boolean hasValidRunes = KnowledgeHelper.isValidRuneString(runes);
        WritingDesks.writtenRunes.put(uuid, runes);

        if (hasValidRunes) {
            createResult(serverPlayer, runes);
        } else {
            clearResult();
        }

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
