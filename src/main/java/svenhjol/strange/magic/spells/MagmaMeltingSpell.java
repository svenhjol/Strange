package svenhjol.strange.magic.spells;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MagmaMeltingSpell extends Spell
{
    public MagmaMeltingSpell()
    {
        super("magma_melting");
        this.element = Element.FIRE;
        this.effect = Effect.AREA;
    }

    @Override
    public boolean cast(PlayerEntity player, ItemStack staff)
    {
        this.castArea(player, 4, blocks -> {
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
                    ((ServerWorld)world).spawnParticle(ParticleTypes.LAVA, pos.getX(), pos.getY(), pos.getZ(), 10, 0, 0, 0, 0.6D);
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
