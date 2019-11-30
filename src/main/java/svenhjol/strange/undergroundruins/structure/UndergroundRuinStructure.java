package svenhjol.strange.undergroundruins.structure;

import com.mojang.datafixers.Dynamic;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
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
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.base.StrangeTemplateStructurePiece;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.undergroundruins.module.UndergroundRuins;

import java.util.*;
import java.util.function.Function;

public class UndergroundRuinStructure extends ScatteredStructure<UndergroundRuinConfig>
{
    public static final int SEED_MODIFIER = 135318;
    public static final String STRUCTURE_NAME = "Underground_Ruin";
    public static IStructurePieceType UNDERGROUND_RUIN_PIECE = UndergroundRuinPiece::new;
    public static Map<Biome.Category, List<ResourceLocation>> biomeRuins = new HashMap<>();

    public UndergroundRuinStructure(Function<Dynamic<?>, ? extends UndergroundRuinConfig> config)
    {
        super(config);
    }

    @Override
    public String getStructureName()
    {
        return STRUCTURE_NAME;
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    @Override
    protected int getSeedModifier()
    {
        return SEED_MODIFIER;
    }

    @Override
    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z)
    {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            int px = x >> 4;
            int pz = z >> 4;

            rand.setSeed((long)(px ^ pz << 4) ^ gen.getSeed());
            rand.nextInt();

            if (rand.nextInt(1) > 0) return false;

            Biome biome = gen.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));
            return gen.hasStructure(biome, UndergroundRuins.structure);
        }

        return false;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return UndergroundRuinStructure.Start::new;
    }

    public static class Start extends StructureStart
    {
        public Start(Structure<?> structure, int chunkX, int chunkZ, Biome biome, MutableBoundingBox bb, int ref, long seed)
        {
            super(structure, chunkX, chunkZ, biome, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> generator, TemplateManager templates, int chunkX, int chunkZ, Biome biome)
        {
            BlockPos pos = new BlockPos(chunkX * 16,  rand.nextInt(12) + 24, chunkZ * 16);

            if (pos.getY() == 0 || pos.getY() > 48) {
                pos = new BlockPos(pos.getX(), rand.nextInt(12) + 24, pos.getZ());
            }
            if (biomeRuins.containsKey(biome.getCategory()) && !biomeRuins.get(biome.getCategory()).isEmpty()) {
                List<ResourceLocation> biomeTemplates = biomeRuins.get(biome.getCategory());
                ResourceLocation useTemplate = biomeTemplates.get(rand.nextInt(biomeTemplates.size()));
                UndergroundRuinPiece ruin = new UndergroundRuinPiece(templates, useTemplate, pos, Rotation.randomRotation(rand));
                components.add(ruin);

                this.recalculateStructureSize();
            }
        }
    }

    public static class UndergroundRuinPiece extends StrangeTemplateStructurePiece
    {
        public UndergroundRuinPiece(TemplateManager templates, ResourceLocation template, BlockPos pos, Rotation rotation)
        {
            super(UNDERGROUND_RUIN_PIECE, 0);

            this.templateName = template;
            this.templatePosition = pos;
            this.integrity = 1.0F;
            this.rotation = rotation;
            this.setup(templates);
        }

        public UndergroundRuinPiece(TemplateManager templates, CompoundNBT tag)
        {
            super(UNDERGROUND_RUIN_PIECE, tag);

            this.setup(templates);
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

            } else if (data.equals("ore")) {

                if (f < 0.2F) {
                    state = Blocks.DIAMOND_ORE.getDefaultState();
                } else if (f < 0.4F) {
                    state = Blocks.EMERALD_ORE.getDefaultState();
                } else if (f < 0.7F) {
                    state = Blocks.GOLD_ORE.getDefaultState();
                } else if (f < 1.0F) {
                    state = Blocks.IRON_ORE.getDefaultState();
                }

            } else if (data.equals("storage")) {

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

                    ResourceLocation lootTable = StrangeLoot.CHESTS_VAULT_STORAGE;

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

                    ResourceLocation lootTable = LootTables.CHESTS_STRONGHOLD_LIBRARY;

                    if (data.contains("vault")) {
                        lootTable = StrangeLoot.CHESTS_VAULT_BOOKSHELVES;
                    }

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

            world.setBlockState(pos, state, 2);
        }
    }
}
