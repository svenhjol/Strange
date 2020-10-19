package svenhjol.strange.block;

import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;
import svenhjol.charm.mixin.accessor.ShovelItemAccessor;
import svenhjol.strange.base.StrangeLoot;

import java.util.List;
import java.util.Random;

public class AncientRubbleBlock extends CharmBlock {
    public AncientRubbleBlock(CharmModule module) {
        super(module, "ancient_rubble", AbstractBlock.Settings.of(Material.AGGREGATE, MaterialColor.STONE)
            .strength(25.0F)
            .requiresTool()
            .sounds(BlockSoundGroup.GRAVEL));

        ShovelItemAccessor.getEffectiveBlocks().add(this);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.isClient)
            return;

        ServerWorld serverWorld = (ServerWorld)world;

        ItemStack held = player.getStackInHand(Hand.MAIN_HAND);

        if ((held.getItem() instanceof ShovelItem && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, held) > 0) || player.isCreative()) {
            float chance = 0.05F;

            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, held);
            int fortune = EnchantmentHelper.getLevel(Enchantments.FORTUNE, held);

            chance += (efficiency * 0.01F);
            chance += (fortune * 0.1F);
            chance += (player.getLuck() * 0.16F);

            if (world.random.nextFloat() < chance || player.isCreative()) {
                dropTreasure(player, serverWorld, pos);
                return;
            }
        }

        dropGravel(serverWorld, pos);
    }

    private void dropGravel(ServerWorld world, BlockPos pos) {
        drop(world, pos, new ItemStack(Blocks.GRAVEL));
    }

    private void dropTreasure(PlayerEntity player, ServerWorld world, BlockPos pos) {
        Random rand = world.random;
        LootTable lootTable = world.getServer().getLootManager().getTable(StrangeLoot.ANCIENT_RUBBLE);
        List<ItemStack> list = lootTable.generateLoot((new LootContext.Builder(world))
            .parameter(LootContextParameters.THIS_ENTITY, player)
            .parameter(LootContextParameters.ORIGIN, player.getPos())
            .random(rand)
            .build(LootContextTypes.CHEST));

        if (!list.isEmpty()) {
            world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 0.8F);
            drop(world, pos, list.get(rand.nextInt(list.size())));
        } else {
            Charm.LOG.warn("Could not get item from loot table");
            dropGravel(world, pos);
        }
    }

    private void drop(ServerWorld world, BlockPos pos, ItemStack stack) {
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 0.8F);
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, stack);
        world.spawnEntity(entity);
    }
}
