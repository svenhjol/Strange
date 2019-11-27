package svenhjol.strange.magic.spells;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class LavaFreezingSpell extends Spell
{
    public LavaFreezingSpell()
    {
        super("lava_freezing");
        this.element = Element.WATER;
        this.affect = Affect.AREA;
        this.duration = 100;
        this.castCost = 80;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> onCast)
    {
        this.castArea(player, new int[] { 5, 1, 5 }, blocks -> {
            World world = player.world;

            if (world.isRemote) return;
            boolean didAnyFreeze = false;

            for (BlockPos pos : blocks) {
                boolean didFreeze = false;
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();

                if (state == Blocks.LAVA.getDefaultState()) {
                    world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), 2);
                    didFreeze = true;
                }

                if (didFreeze) {
                    didAnyFreeze = true;
                }
            }

            if (didAnyFreeze) {
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, SoundCategory.BLOCKS, 1.0F, 0.9F);
            }
        });

        onCast.accept(true);
    }
}
