package svenhjol.strange.magic.spells;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MagmaBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MagmaMeltingSpell extends Spell
{
    public MagmaMeltingSpell()
    {
        super("magma_melting");
        this.element = Element.FIRE;
        this.affect = Affect.AREA;
        this.duration = 100;
        this.xpCost = 80;
    }

    @Override
    public boolean cast(PlayerEntity player)
    {
        this.castArea(player, new int[] { 4, 3, 4 }, blocks -> {
            World world = player.world;

            if (world.isRemote) return;
            boolean didAnyMelt = false;

            for (BlockPos pos : blocks) {
                boolean didMelt = false;
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();

                if (block instanceof MagmaBlock) {
                    world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 2);
                    didMelt = true;
                }

                if (didMelt) {
                    didAnyMelt = true;
                }
            }

            if (didAnyMelt) {
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.7F, 0.8F);
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, SoundCategory.BLOCKS, 1.0F, 0.9F);
            }
        });

        return true;
    }
}
