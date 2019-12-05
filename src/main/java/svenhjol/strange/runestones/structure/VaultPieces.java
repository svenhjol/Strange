package svenhjol.strange.runestones.structure;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTables;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.base.StrangeTemplateStructurePiece;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static svenhjol.strange.runestones.structure.VaultPieces.VaultPieceType.*;
import static svenhjol.strange.runestones.structure.VaultStructure.VAULT_PIECE;

public class VaultPieces
{
    public enum VaultPieceType
    {
        Corridor,
        Junction,
        Large
    }

    static Map<VaultPieceType, int[]> sizes = new HashMap<>();
    static {
        sizes.put(Corridor, new int[]{5, 8, 11});
        sizes.put(Junction, new int[]{9, 8, 9});
        sizes.put(Large, new int[]{17, 15, 17});
    }

    public static class VaultPiece extends StrangeTemplateStructurePiece
    {
        public VaultPiece(TemplateManager templates, ResourceLocation template, BlockPos pos, Rotation rotation)
        {
            super(VAULT_PIECE, 0);

            this.templateName = template;
            this.templatePosition = pos;
            this.integrity = 1.0F;
            this.rotation = rotation;
            this.setup(templates);
        }

        public VaultPiece(TemplateManager templates, CompoundNBT tag)
        {
            super(VAULT_PIECE, tag);

            this.setup(templates);
        }

        @Override
        protected void dataForBookshelf(IWorld world, BlockPos pos, String data, Random rand)
        {
            super.dataForBookshelf(world, pos, data, rand);

            if (world.getTileEntity(pos) != null) {
                ResourceLocation lootTable;
                if (rand.nextFloat() < 0.5F) {
                    lootTable = StrangeLoot.CHESTS_VAULT_BOOKSHELVES;
                } else {
                    lootTable = LootTables.CHESTS_STRONGHOLD_LIBRARY;
                }
                LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
            }
        }

        @Override
        protected void dataForChest(IWorld world, BlockPos pos, String data, Random rand)
        {
            super.dataForChest(world, pos, data, rand);

            if (world.getTileEntity(pos) != null) {
                ResourceLocation lootTable = StrangeLoot.CHESTS_VAULT_TREASURE;
                if (data.contains("stronghold")) {
                    lootTable = LootTables.CHESTS_STRONGHOLD_LIBRARY;
                }
                LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
            }
        }

        @Override
        protected void dataForMob(IWorld world, BlockPos pos, String data, Random rand)
        {
            if (rand.nextFloat() < 0.85F) {
                MobEntity mob = null;
                float f = rand.nextFloat();

                if (f < 0.12F) {
                    mob = EntityType.ILLUSIONER.create(world.getWorld());
                } else if (f < 0.24F) {
                    mob = EntityType.EVOKER.create(world.getWorld());
                } else if (f < 0.55F) {
                    mob = EntityType.VINDICATOR.create(world.getWorld());
                } else if (f < 0.8F) {
                    mob = EntityType.PILLAGER.create(world.getWorld());
                } else {
                    mob = EntityType.WITCH.create(world.getWorld());
                }

                if (mob == null) return;

                mob.enablePersistence();
                mob.moveToBlockPosAndAngles(pos, 0.0F, 0.0F);
                mob.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, null, null);
                world.addEntity(mob);
            }
        }

        @Override
        protected void dataForStorage(IWorld world, BlockPos pos, String data, Random rand)
        {
            super.dataForStorage(world, pos, data, rand);

            if (world.getTileEntity(pos) != null) {
                LockableLootTileEntity.setLootTable(world, rand, pos, StrangeLoot.CHESTS_VAULT_STORAGE);
            }
        }
    }
}
