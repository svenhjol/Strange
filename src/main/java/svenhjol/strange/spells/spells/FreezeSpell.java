package svenhjol.strange.spells.spells;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FreezeSpell extends Spell
{
    public FreezeSpell()
    {
        super("freeze");
        this.color = DyeColor.LIGHT_BLUE;
        this.affect = Affect.AREA;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        int[] range = { 6, 4, 6 };
        this.castArea(player, range, blocks -> {
            World world = player.world;
            if (world.isRemote) return;

            AxisAlignedBB area = player.getBoundingBox().grow(range[0], range[1], range[2]);

            List<MonsterEntity> monsters;
            Predicate<MonsterEntity> selector = e -> !e.isEntityEqual(player);
            monsters = world.getEntitiesWithinAABB(MonsterEntity.class, area, selector);

            if (!monsters.isEmpty()) {
                for (MonsterEntity monster : monsters) {
                    monster.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 80, 3));
                }
            }

            List<LivingEntity> entities;
            Predicate<LivingEntity> livingSelector = Objects::nonNull;
            entities = world.getEntitiesWithinAABB(LivingEntity.class, area, livingSelector);

            if (!entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    if (entity.isBurning()) {
                        entity.extinguish();
                    }
                }
            }

            boolean didAnyFreeze = false;

            for (BlockPos pos : blocks) {
                boolean didFreeze = false;
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();

                if (state == Blocks.LAVA.getDefaultState()) {
                    world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state.getBlock() == Blocks.LAVA) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state == Blocks.WATER.getDefaultState()) {
                    world.setBlockState(pos, Blocks.ICE.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state == Blocks.FIRE.getDefaultState()) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    didFreeze = true;
                } else if (block instanceof FireBlock) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state.getBlock() == Blocks.WATER) {
                    world.setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state.isSolid()
                    && state.getMaterial() != Material.SNOW
                    && world.isAirBlock(pos.up())
                    && world.rand.nextFloat() < 0.3F
                ) {
                    world.setBlockState(pos.up(), Blocks.SNOW.getDefaultState(), 2);
                }

                if (didFreeze) {
                    didAnyFreeze = true;
                    world.neighborChanged(pos, state.getBlock(), pos);
                }
            }

            if (didAnyFreeze) {
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, SoundCategory.BLOCKS, 1.0F, 0.9F);
            }
        });

        didCast.accept(true);
    }
}
