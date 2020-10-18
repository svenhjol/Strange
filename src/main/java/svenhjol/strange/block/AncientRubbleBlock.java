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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;
import svenhjol.charm.mixin.accessor.ShovelItemAccessor;
import svenhjol.strange.base.StrangeLoot;

import java.util.List;

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

        if (held.getItem() instanceof ShovelItem
            && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, held) > 0
        ) {
            float chance = 0.05F;

            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, held);
            int fortune = EnchantmentHelper.getLevel(Enchantments.FORTUNE, held);

            chance += (efficiency * 0.01F);
            chance += (fortune * 0.1F);
            chance += (player.getLuck() * 0.1F);

            if (world.random.nextFloat() < chance)
                dropTreasure(player, serverWorld, pos);
        }

        dropGravel(player, serverWorld, pos);
    }

    private void dropGravel(PlayerEntity player, ServerWorld world, BlockPos pos) {
        drop((ServerWorld)player.world, pos, new ItemStack(Blocks.GRAVEL));
    }

    private void dropTreasure(PlayerEntity player, ServerWorld world, BlockPos pos) {
        LootTable lootTable = world.getServer().getLootManager().getTable(StrangeLoot.ANCIENT_RUBBLE);
        List<ItemStack> list = lootTable.generateLoot((new LootContext.Builder(world))
            .parameter(LootContextParameters.THIS_ENTITY, player)
            .parameter(LootContextParameters.ORIGIN, player.getPos())
            .random(world.random)
            .build(LootContextTypes.CHEST));

        if (!list.isEmpty()) {
            drop(world, pos, list.get(0));
        } else {
            Charm.LOG.warn("Could not get item from loot table");
            dropGravel(player, world, pos);
        }
    }

    private void drop(ServerWorld world, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, stack);
        world.spawnEntity(entity);
    }
}
