package svenhjol.strange.runictablets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.strange.runestones.RunestoneHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class RunicAltarScreenHandler extends ScreenHandler {
    private final Inventory input;
    private final Inventory result;
    private final PlayerInventory playerInventory;
    private final PlayerEntity player;
    private final ScreenHandlerContext context;

    public RunicAltarScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public RunicAltarScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(RunicTablets.SCREEN_HANDLER, syncId);

        this.playerInventory = playerInventory;
        this.player = playerInventory.player;
        this.result = new CraftingResultInventory();
        this.input = new SimpleInventory(4) {
            public void markDirty() {
                super.markDirty();
                RunicAltarScreenHandler.this.onContentChanged(this);
            }
        };
        this.context = context;

        this.addSlot(new Slot(this.input, 0, 26, 24) {
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == RunicTablets.RUNIC_FRAGMENT
                    || stack.getItem() == RunicTablets.RUNIC_TABLET
                    || stack.getItem() == Items.COMPASS;
            }
        });
        this.addSlot(new Slot(this.input, 1, 26, 45) {
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == Items.CLAY_BALL;
            }
        });

        this.addSlot(new Slot(this.result, 4, 134, 35) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
                context.run((world, pos) -> {
                    // might want to do something at the block position
                    world.playSound(null, pos, SoundEvents.ENTITY_CAT_HISS, SoundCategory.BLOCKS, 1.0F, 1.0F);
                });
                for (int i = 0; i < 2; i++) {
                    RunicAltarScreenHandler.this.input.getStack(i).decrement(1);
                }
                player.addExperienceLevels(-RunicTablets.requiredXpLevels);
                return stack;
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, RunicTablets.RUNIC_ALTAR);
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
        if (player == null)
            return;

        this.result.setStack(0, ItemStack.EMPTY);
        ItemStack stack0 = this.input.getStack(0);
        BlockPos pos = getPosFromStack(stack0, player.world);

        if (pos == null)
            return;

        ItemStack stack1 = this.input.getStack(1);
        if (stack1.getItem() != Items.CLAY_BALL)
            return;

        List<Integer> runesFromBlockPos = RunestoneHelper.getRunesFromBlockPos(pos, 6);

        if (!player.isCreative()) {

            // the player must have these runes to create a runic tablet
            List<Integer> discovered = RunestoneHelper.getLearnedRunes(player);

            for (int rune : runesFromBlockPos) {
                if (!discovered.contains(rune))
                    return;
            }

            // the player must have enough XP to create a runic tablet
            if (player.experienceLevel < RunicTablets.requiredXpLevels)
                return;
        }

        ItemStack out = new ItemStack(RunicTablets.RUNIC_TABLET);
        RunicTabletItem.setPos(out, pos);
        RunicTabletItem.setDimension(out, DimensionHelper.getDimension(player.world));
        out.setCustomName(getNameFromStack(stack0));
        this.result.setStack(0, out);
        this.sendContentUpdates();
    }

    @Nullable
    public Text getNameFromStack(ItemStack stack) {
        Text text = null;
        Item item = stack.getItem();

        if (item == RunicTablets.RUNIC_FRAGMENT) {
            text = new TranslatableText("item.strange.runic_tablet_bound", stack.getName());
        } else if (item == Items.COMPASS) {
            TranslatableText lodestone = new TranslatableText("block.minecraft.lodestone");
            text = new TranslatableText("item.strange.runic_tablet_bound", lodestone);
        }

        return text;
    }

    @Nullable
    public BlockPos getPosFromStack(ItemStack stack, World world) {
        BlockPos pos = null;

        if (stack.getItem() == RunicTablets.RUNIC_FRAGMENT) {
            pos = RunicFragmentItem.getPos(stack);

            // must be in correct dimension as the fragment
            Identifier dimension = RunicFragmentItem.getDimension(stack);
            if (dimension != null && !DimensionHelper.isDimension(world, dimension))
                return null;

            if (!world.isClient) {
                if (RunicFragmentItem.isWaitingForTimeout(stack, world))
                    return null;

                // try and generate a pos for this runic fragment
                if (pos == null) {
                    boolean result = RunicFragmentItem.populate(stack, (ServerWorld) world, player.getBlockPos(), world.random);
                    if (!result)
                        return null;
                }

                pos = RunicFragmentItem.getPos(stack);
            }

        } else if (stack.getItem() == Items.COMPASS) {
            if (!CompassItem.hasLodestone(stack) || !stack.hasTag() || stack.getTag() == null)
                return null;

            // must be the correct dimension as the lodestone
            Optional<RegistryKey<World>> dimension = CompassItem.getLodestoneDimension(stack.getTag());
            if (!dimension.isPresent() || !DimensionHelper.isDimension(world, dimension.get()))
                return null;

            pos = NbtHelper.toBlockPos(stack.getTag().getCompound("LodestonePos"));
            pos = pos.add(0, 1, 0); // the block above the lodestone
        }

        return pos;
    }
}
