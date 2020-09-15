package svenhjol.strange.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.BlockView;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlockWithEntity;
import svenhjol.strange.blockentity.EntitySpawnerBlockEntity;

import javax.annotation.Nullable;

public class EntitySpawnerBlock extends MesonBlockWithEntity {
    public EntitySpawnerBlock(MesonModule module) {
        super(module, "entity_spawner", AbstractBlock.Settings
            .of(Material.AIR, MaterialColor.BLACK)
            .strength(-1.0F, 3400000.0F)
            .dropsNothing());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new EntitySpawnerBlockEntity();
    }

    @Override
    public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> list) {
        // don't
    }
}
