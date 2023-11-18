package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

public class CookingPotContainers {
    static class InputContainer
        extends SimpleContainer
        implements WorldlyContainer {
        private final LevelAccessor level;
        private final BlockPos pos;

        public InputContainer(LevelAccessor level, BlockPos pos) {
            super(1);
            this.level = level;
            this.pos = pos;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            int[] slots;
            if (direction == Direction.UP) {
                slots = new int[1];
            } else {
                slots = new int[]{};
            }
            return slots;
        }

        @Override
        public boolean canPlaceItemThroughFace(int i, ItemStack stack, @Nullable Direction direction) {
            return direction == Direction.UP && stack.isEdible();
        }

        @Override
        public boolean canTakeItemThroughFace(int i, ItemStack stack, Direction direction) {
            return false;
        }

        @Override
        public void setChanged() {
            var stack = this.getItem(0);
            if (!stack.isEmpty()) {
                if (this.level.getBlockEntity(this.pos) instanceof CookingPotBlockEntity cookingPot) {
                    cookingPot.add(stack);
                    this.removeItemNoUpdate(0);
                }
            }
        }
    }


    static class EmptyContainer
        extends SimpleContainer
        implements WorldlyContainer {
        public EmptyContainer() {
            super(0);
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            return new int[0];
        }

        @Override
        public boolean canPlaceItemThroughFace(int i, ItemStack stack, @Nullable Direction direction) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int i, ItemStack stack, Direction direction) {
            return false;
        }
    }
}
