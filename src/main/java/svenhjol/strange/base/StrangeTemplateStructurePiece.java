package svenhjol.strange.base;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.*;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.charm.Charm;
import svenhjol.charm.decoration.block.BookshelfChestBlock;
import svenhjol.charm.decoration.module.AllTheBarrels;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.charm.decoration.module.Crates;
import svenhjol.charm.decoration.module.GoldLanterns;
import svenhjol.meson.enums.WoodType;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.module.Runestones;

import java.util.*;

public abstract class StrangeTemplateStructurePiece extends TemplateStructurePiece
{
    protected ResourceLocation templateName;
    protected float integrity;
    protected Rotation rotation;
    protected int x;
    protected int y;
    protected int z;

    public List<Block> flowerTypes = Arrays.asList(
        Blocks.SUNFLOWER,
        Blocks.POPPY,
        Blocks.AZURE_BLUET,
        Blocks.WHITE_TULIP,
        Blocks.LILAC
    );

    public StrangeTemplateStructurePiece(IStructurePieceType piece, int type)
    {
        super(piece, type);
    }

    public StrangeTemplateStructurePiece(IStructurePieceType piece, CompoundNBT tag)
    {
        super(piece, tag);

        this.templateName = new ResourceLocation(tag.getString("Template"));
        this.integrity = tag.getFloat("Integrity");
        this.rotation = Rotation.valueOf(tag.getString("Rotation"));
    }

    protected void setup(TemplateManager templates)
    {
        Template template = templates.getTemplateDefaulted(this.templateName);
        PlacementSettings placement = (new PlacementSettings())
            .setRotation(this.rotation)
            .setMirror(Mirror.NONE)
            .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);

        BlockPos size = template.getSize();
        this.x = size.getX();
        this.y = size.getY();
        this.z = size.getZ();

        this.setup(template, this.templatePosition, placement);
    }

    public BlockPos getTemplatePosition()
    {
        return this.templatePosition;
    }

    @Override
    protected void readAdditional(CompoundNBT tag)
    {
        super.readAdditional(tag);
        tag.putString("Template", this.templateName.toString());
        tag.putFloat("Integrity", this.integrity);
        tag.putString("Rotation", this.rotation.name());
    }

    @Override
    public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunkPos)
    {
        this.placeSettings
            .clearProcessors()
            .addProcessor(new IntegrityProcessor(this.integrity))
            .addProcessor(new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.STRUCTURE_BLOCK, Blocks.LIGHT_BLUE_STAINED_GLASS)));

        BlockPos pos = new BlockPos(this.templatePosition.getX(), this.templatePosition.getY(), this.templatePosition.getZ());

        this.templatePosition = pos;
        return super.addComponentParts(world, rand, bb, chunkPos);
    }

    @Override
    protected void handleDataMarker(String data, BlockPos pos, IWorld world, Random rand, MutableBoundingBox bb)
    {
        BlockState state = Blocks.AIR.getDefaultState();
        float f = rand.nextFloat();
        List<WoodType> woodTypes = new ArrayList<>(Arrays.asList(WoodType.values()));
        WoodType woodType = woodTypes.get(rand.nextInt(woodTypes.size()));

        if (data.contains("chest")) {

            if (f < 0.66F) {
                state = Blocks.CHEST.getDefaultState();

                ResourceLocation lootTable = LootTables.CHESTS_SIMPLE_DUNGEON;

                Set<ResourceLocation> lootTables = LootTables.func_215796_a();
                for (ResourceLocation res : lootTables) {
                    if (data.contains(res.getPath())) {
                        lootTable = res;
                    }
                }

                if (data.contains("north")) state = state.with(ChestBlock.FACING, Direction.NORTH);
                if (data.contains("east")) state = state.with(ChestBlock.FACING, Direction.EAST);
                if (data.contains("south")) state = state.with(ChestBlock.FACING, Direction.SOUTH);
                if (data.contains("west")) state = state.with(ChestBlock.FACING, Direction.WEST);
                if (data.contains("water")) state = state.with(ChestBlock.WATERLOGGED, true);

                world.setBlockState(pos, state, 2);
                LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
                return;
            }

        } else if (data.contains("mob")) {

            Entity entity = null;
            EntityType<?> type = null;
            boolean persist = data.contains("persist");

            List<ResourceLocation> entities = new ArrayList<>(ForgeRegistries.ENTITIES.getKeys());

            for (ResourceLocation res : entities) {
                if (data.contains(res.getPath())) {
                    type = ForgeRegistries.ENTITIES.getValue(res);
                }
            }

            if (type == null) return;
            entity = type.create(world.getWorld());

            if (entity == null) return;
            entity.moveToBlockPosAndAngles(pos, 0.0F, 0.0F);

            if (entity instanceof MobEntity) {
                if (persist) ((MobEntity)entity).enablePersistence();
                ((MobEntity)entity).onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, null, null);
            }
            world.addEntity(entity);

        } else if (data.contains("lantern")) {

            if (f < 0.5F) {
                state = Blocks.LANTERN.getDefaultState();

                if (Charm.loader.hasModule(GoldLanterns.class) && f < 0.25F) {
                    state = GoldLanterns.block.getDefaultState();
                }

                if (data.contains("hanging")) {
                    state = state.with(LanternBlock.HANGING, true);
                }
            }

        } else if (data.equals("rune")) {

            if (Strange.loader.hasModule(Runestones.class) && f < 0.75F) {
                state = Runestones.getRunestoneBlock(world, rand.nextInt(Runestones.dests.size()));
            }

        } else if (data.contains("ore")) {

            if (f < 0.66F) {

                if (f < 0.2F) {
                    state = Blocks.DIAMOND_ORE.getDefaultState();
                } else if (f < 0.4F) {
                    state = Blocks.EMERALD_ORE.getDefaultState();
                } else if (f < 0.7F) {
                    state = Blocks.GOLD_ORE.getDefaultState();
                } else if (f < 1.0F) {
                    state = Blocks.IRON_ORE.getDefaultState();
                }
            }

        } else if (data.contains("storage")) {

            if (f < 0.66F) {

                List<BlockState> types = new ArrayList<>(Arrays.asList(
                    Blocks.BARREL.getDefaultState().with(BarrelBlock.PROPERTY_FACING, Direction.UP)
                ));
                if (Charm.loader.hasModule(AllTheBarrels.class)) {
                    types.add(AllTheBarrels.barrels.get(rand.nextInt(AllTheBarrels.barrels.size())).getDefaultState()
                        .with(BarrelBlock.PROPERTY_FACING, Direction.UP));
                }
                if (Charm.loader.hasModule(Crates.class)) {
                    types.add(Crates.openTypes.get(woodType).getDefaultState());
                    types.add(Crates.sealedTypes.get(woodType).getDefaultState());
                }

                state = types.get(rand.nextInt(types.size()));

                List<ResourceLocation> tables = Arrays.asList(
                    LootTables.CHESTS_SHIPWRECK_SUPPLY,
                    LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE,
                    LootTables.CHESTS_IGLOO_CHEST
                );
                ResourceLocation lootTable = getLootTableResourceLocation(data, tables.get(rand.nextInt(tables.size())));

                world.setBlockState(pos, state, 2);
                LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
                return;
            }

        } else if (data.contains("lectern")) {

            state = Blocks.LECTERN.getDefaultState();
            if (data.contains("north")) state = state.with(LecternBlock.FACING, Direction.SOUTH);
            if (data.contains("east")) state = state.with(LecternBlock.FACING, Direction.WEST);
            if (data.contains("south")) state = state.with(LecternBlock.FACING, Direction.NORTH);
            if (data.contains("west")) state = state.with(LecternBlock.FACING, Direction.EAST);

        } else if (data.contains("bookshelf")) {

            if (Charm.loader.hasModule(BookshelfChests.class) && f < 0.22F) {
                state = ((Block) BookshelfChests.blocks.get(WoodType.OAK)).getDefaultState()
                    .with(BookshelfChestBlock.SLOTS, 9);

                ResourceLocation lootTable = getLootTableResourceLocation(data, LootTables.CHESTS_STRONGHOLD_LIBRARY);

                world.setBlockState(pos, state, 2);
                LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
                return;

            } else {
                state = Blocks.BOOKSHELF.getDefaultState();
            }

        } else if (data.equals("bars")) {

            if (f < 0.8F) {
                state = Blocks.IRON_BARS.getDefaultState();
            }

        } else if (data.equals("vines")) {

            if (f < 0.8F) {
                state = Blocks.VINE.getDefaultState();
            }

        } else if (data.contains("flower")) {

            if (f < 0.8F) {
                state = flowerTypes.get(rand.nextInt(flowerTypes.size())).getDefaultState();
            }

        } else if (data.equals("spawner")) {

            if (f < 0.8F) {
                state = Blocks.SPAWNER.getDefaultState();
                world.setBlockState(pos, state, 2);
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof MobSpawnerTileEntity) {
                    ((MobSpawnerTileEntity) tile).getSpawnerBaseLogic().setEntityType(DungeonHooks.getRandomDungeonMob(rand));
                }
            }

        } else if (data.contains("decoration")) {

            if (f < 0.5F) {

                Direction facing = Direction.NORTH;
                if (data.contains("east")) facing = Direction.EAST;
                if (data.contains("south")) facing = Direction.SOUTH;
                if (data.contains("west")) facing = Direction.WEST;

                List<BlockState> types = new ArrayList<>(Arrays.asList(
                    Blocks.BLAST_FURNACE.getDefaultState().with(BlastFurnaceBlock.FACING, facing),
                    Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, facing),
                    Blocks.SMOKER.getDefaultState().with(SmokerBlock.FACING, facing),
                    Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, facing),
                    Blocks.LECTERN.getDefaultState().with(LecternBlock.FACING, facing),
                    Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, false),
                    Blocks.CAULDRON.getDefaultState().with(CauldronBlock.LEVEL, rand.nextInt(3)),
                    Blocks.COMPOSTER.getDefaultState().with(ComposterBlock.LEVEL, rand.nextInt(7)),
                    Blocks.PUMPKIN.getDefaultState(),
                    Blocks.MELON.getDefaultState(),
                    Blocks.LANTERN.getDefaultState(),
                    Blocks.COBWEB.getDefaultState(),
                    Blocks.HAY_BLOCK.getDefaultState(),
                    Blocks.JUKEBOX.getDefaultState(),
                    Blocks.NOTE_BLOCK.getDefaultState(),
                    Blocks.FLETCHING_TABLE.getDefaultState(),
                    Blocks.SMITHING_TABLE.getDefaultState(),
                    Blocks.CRAFTING_TABLE.getDefaultState(),
                    Blocks.CARTOGRAPHY_TABLE.getDefaultState(),
                    Blocks.ANVIL.getDefaultState(),
                    Blocks.CHIPPED_ANVIL.getDefaultState(),
                    Blocks.DAMAGED_ANVIL.getDefaultState(),
                    Blocks.BELL.getDefaultState()
                ));

                state = types.get(rand.nextInt(types.size()));
            }
        }

        state = state.rotate(this.rotation);
        world.setBlockState(pos, state, 2);
    }

    public ResourceLocation getLootTableResourceLocation(String name, ResourceLocation defaultTable)
    {
        ResourceLocation lootTable = defaultTable;

        Set<ResourceLocation> lootTables = LootTables.func_215796_a();
        for (ResourceLocation res : lootTables) {
            if (name.contains(res.getPath())) {
                lootTable = res;
            }
        }

        return lootTable;
    }
}
