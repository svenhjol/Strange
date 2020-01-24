package svenhjol.strange.spells.spells;

import net.minecraft.block.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HeatSpell extends Spell
{
    public HeatSpell()
    {
        super("heat");
        this.color = DyeColor.YELLOW;
        this.affect = Affect.AREA;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        int[] range = { 7, 3, 7 };
        this.castArea(player, range, blocks -> {
            World world = player.world;
            if (world.isRemote) return;

            List<MonsterEntity> entities;
            AxisAlignedBB area = player.getBoundingBox().grow(range[0], range[1], range[2]);
            Predicate<MonsterEntity> selector = e -> !e.isEntityEqual(player);
            entities = world.getEntitiesWithinAABB(MonsterEntity.class, area, selector);

            if (!entities.isEmpty()) {
                for (MonsterEntity entity : entities) {
                    entity.setFire(4);
                }
            }

            boolean didAnyMelt = false;

            for (BlockPos pos : blocks) {
                boolean didMelt = false;
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();

                if (block instanceof MagmaBlock) {
                    world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 2);
                    didMelt = true;
                } else if (block instanceof IceBlock) {
                    world.setBlockState(pos, Blocks.WATER.getDefaultState(), 2);
                    didMelt = true;
                } else if (block instanceof CampfireBlock
                    && !state.get(CampfireBlock.LIT)
                ) {
                    world.setBlockState(pos, state.with(CampfireBlock.LIT, true), 2);
                } else if (block instanceof SnowBlock) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                }

                if (didMelt)
                    didAnyMelt = true;
            }

            if (didAnyMelt) {
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.7F, 0.8F);
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, SoundCategory.BLOCKS, 1.0F, 0.9F);
            }
        });

        didCast.accept(true);
    }
}
