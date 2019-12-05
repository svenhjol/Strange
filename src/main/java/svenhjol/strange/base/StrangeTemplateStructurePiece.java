package svenhjol.strange.base;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
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
import svenhjol.strange.scrolls.block.WritingDeskBlock;
import svenhjol.strange.scrolls.module.Scrollkeepers;

import java.util.*;

public abstract class StrangeTemplateStructurePiece extends TemplateStructurePiece
{
    public static final String BOOKSHELF = "bookshelf";
    public static final String CARPET = "carpet";
    public static final String CHEST = "chest";
    public static final String DECORATION = "decoration";
    public static final String FLOWER = "flower";
    public static final String LANTERN = "lantern";
    public static final String LECTERN = "lectern";
    public static final String MOB = "mob";
    public static final String ORE = "ore";
    public static final String RUNE = "rune";
    public static final String SAPLING = "sapling";
    public static final String SPAWNER = "spawner";
    public static final String STORAGE = "storage";

    public float integrity;
    public int x;
    public int y;
    public int z;
    protected ResourceLocation templateName;
    protected Rotation rotation;
    protected Random fixedRand;

    protected Direction faceNorth;
    protected Direction faceEast;
    protected Direction faceSouth;
    protected Direction faceWest;

    public List<WoodType> woodTypes = new ArrayList<>(Arrays.asList(WoodType.values()));

    public static List<Block> flowerTypes = Arrays.asList(
        Blocks.SUNFLOWER,
        Blocks.POPPY,
        Blocks.AZURE_BLUET,
        Blocks.WHITE_TULIP,
        Blocks.RED_TULIP,
        Blocks.PINK_TULIP,
        Blocks.ORANGE_TULIP,
        Blocks.BLUE_ORCHID,
        Blocks.OXEYE_DAISY,
        Blocks.WITHER_ROSE,
        Blocks.LILAC,
        Blocks.DANDELION
    );

    public static List<Block> saplingTypes = Arrays.asList(
        Blocks.OAK_SAPLING,
        Blocks.SPRUCE_SAPLING,
        Blocks.BIRCH_SAPLING,
        Blocks.ACACIA_SAPLING,
        Blocks.DARK_OAK_SAPLING,
        Blocks.JUNGLE_SAPLING
    );

    public static List<Block> oreTypes = Arrays.asList(
        Blocks.DIAMOND_ORE,
        Blocks.EMERALD_ORE,
        Blocks.GOLD_ORE,
        Blocks.IRON_ORE,
        Blocks.COAL_ORE,
        Blocks.REDSTONE_ORE,
        Blocks.LAPIS_ORE
    );

    public static List<Block> carpetTypes = Arrays.asList(
        Blocks.WHITE_CARPET,
        Blocks.ORANGE_CARPET,
        Blocks.RED_CARPET,
        Blocks.YELLOW_CARPET,
        Blocks.LIME_CARPET,
        Blocks.CYAN_CARPET,
        Blocks.BLUE_CARPET,
        Blocks.PURPLE_CARPET,
        Blocks.BLACK_CARPET
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

        this.templatePosition = new BlockPos(this.templatePosition.getX(), this.templatePosition.getY(), this.templatePosition.getZ());
        return super.addComponentParts(world, rand, bb, chunkPos);
    }

    @Override
    protected void handleDataMarker(String data, BlockPos pos, IWorld world, Random rand, MutableBoundingBox bb)
    {
        this.fixedRand = new Random();
        this.fixedRand.setSeed(bb.hashCode());

        this.faceNorth = rotation.rotate(Direction.NORTH);
        this.faceEast = rotation.rotate(Direction.EAST);
        this.faceSouth = rotation.rotate(Direction.SOUTH);
        this.faceWest = rotation.rotate(Direction.WEST);

        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 1);
        rand.setSeed(pos.toLong());

        if (data.contains(BOOKSHELF)) dataForBookshelf(world, pos, data, rand);
        if (data.contains(CARPET)) dataForCarpet(world, pos, data, rand);
        if (data.contains(CHEST)) dataForChest(world, pos, data, rand);
        if (data.contains(DECORATION)) dataForDecoration(world, pos, data, rand);
        if (data.contains(FLOWER)) dataForFlower(world, pos, data, rand);
        if (data.contains(LANTERN)) dataForLantern(world, pos, data, rand);
        if (data.contains(LECTERN)) dataForLectern(world, pos, data, rand);
        if (data.contains(MOB)) dataForMob(world, pos, data, rand);
        if (data.contains(ORE)) dataForOre(world, pos, data, rand);
        if (data.contains(RUNE)) dataForRune(world, pos, data, rand);
        if (data.contains(SAPLING)) dataForSapling(world, pos, data, rand);
        if (data.contains(SPAWNER)) dataForSpawner(world, pos, data, rand);
        if (data.contains(STORAGE)) dataForStorage(world, pos, data, rand);
    }

    protected void dataForBookshelf(IWorld world, BlockPos pos, String data, Random rand)
    {
        // TODO variant bookshelves
        if (Charm.loader.hasModule(BookshelfChests.class) && rand.nextFloat() < 0.22F) {
            BlockState state = ((Block) BookshelfChests.blocks.get(WoodType.OAK)).getDefaultState()
                .with(BookshelfChestBlock.SLOTS, 9);

            ResourceLocation lootTable = getVanillaLootTableResourceLocation(data, LootTables.CHESTS_STRONGHOLD_LIBRARY);
            world.setBlockState(pos, state, 2);
            LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
        } else {
            BlockState state = Blocks.BOOKSHELF.getDefaultState();
            world.setBlockState(pos, state, 2);
        }
    }

    protected void dataForCarpet(IWorld world, BlockPos pos, String data, Random rand)
    {
        List<Block> types = new ArrayList<>(carpetTypes);
        Collections.shuffle(types);
        BlockState state = null;

        for (int i = 0; i < types.size(); i++) {
            if (data.contains(String.valueOf(i)) && types.contains(i)) {
                state = types.get(i).getDefaultState();
            }
        }

        if (state != null) {
            world.setBlockState(pos, state, 2);
        }
    }

    protected void dataForChest(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.66F) {
            BlockState state = Blocks.CHEST.getDefaultState();

            ResourceLocation lootTable = LootTables.CHESTS_SIMPLE_DUNGEON;

            Set<ResourceLocation> lootTables = LootTables.func_215796_a();
            for (ResourceLocation res : lootTables) {
                String[] s = res.getPath().split("/");
                if (data.contains(s[s.length-1])) {
                    lootTable = res;
                }
            }

            if (data.contains("north")) state = state.with(ChestBlock.FACING, faceNorth);
            if (data.contains("east")) state = state.with(ChestBlock.FACING, faceEast);
            if (data.contains("south")) state = state.with(ChestBlock.FACING, faceSouth);
            if (data.contains("west")) state = state.with(ChestBlock.FACING, faceWest);

            if (world.getBlockState(pos.up()).getMaterial() == Material.WATER) {
                state = state.with(ChestBlock.WATERLOGGED, true);
            }

            world.setBlockState(pos, state, 2);
            LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
        }
    }

    protected void dataForDecoration(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.65F) {
            Direction facing = faceNorth;
            if (data.contains("east")) facing = faceEast;
            if (data.contains("south")) facing = faceSouth;
            if (data.contains("west")) facing = faceWest;

            List<BlockState> types = new ArrayList<>(Arrays.asList(
                Blocks.BLAST_FURNACE.getDefaultState().with(BlastFurnaceBlock.FACING, facing),
                Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, facing),
                Blocks.SMOKER.getDefaultState().with(SmokerBlock.FACING, facing),
                Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, facing),
                Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, facing),
                Blocks.OBSERVER.getDefaultState().with(ObserverBlock.FACING, facing),
                Blocks.LECTERN.getDefaultState().with(LecternBlock.FACING, facing),
                Blocks.TRAPPED_CHEST.getDefaultState().with(TrappedChestBlock.FACING, facing),
                Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, false),
                Blocks.CAULDRON.getDefaultState().with(CauldronBlock.LEVEL, rand.nextInt(3)),
                Blocks.COMPOSTER.getDefaultState().with(ComposterBlock.LEVEL, rand.nextInt(7)),
                Blocks.BREWING_STAND.getDefaultState(),
                Blocks.STONECUTTER.getDefaultState(),
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

            if (Strange.loader.hasModule(Scrollkeepers.class)) {
                types.add(Scrollkeepers.block.getDefaultState().with(WritingDeskBlock.FACING, facing));
            }

            BlockState state = types.get(rand.nextInt(types.size()));
            world.setBlockState(pos, state, 2);
        }
    }

    protected void dataForFlower(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.8F) {
            BlockState state = flowerTypes.get(rand.nextInt(flowerTypes.size())).getDefaultState();
            world.setBlockState(pos, state, 2);
        }
    }

    protected void dataForLantern(IWorld world, BlockPos pos, String data, Random rand)
    {
        BlockState state = Blocks.LANTERN.getDefaultState();

        if (Charm.loader.hasModule(GoldLanterns.class) && rand.nextFloat() < 0.25F) {
            state = GoldLanterns.block.getDefaultState();
        }

        if (data.contains("hanging")) {
            state = state.with(LanternBlock.HANGING, true);
        }

        world.setBlockState(pos, state, 2);
    }

    protected void dataForLectern(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.8F) {
            BlockState state = Blocks.LECTERN.getDefaultState();
            if (data.contains("north")) state = state.with(LecternBlock.FACING, faceSouth);
            if (data.contains("east")) state = state.with(LecternBlock.FACING, faceWest);
            if (data.contains("south")) state = state.with(LecternBlock.FACING, faceNorth);
            if (data.contains("west")) state = state.with(LecternBlock.FACING, faceEast);

            world.setBlockState(pos, state, 2);
        }
    }

    protected void dataForMob(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.8F) {
            Entity entity;
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
                if (persist) ((MobEntity) entity).enablePersistence();
                ((MobEntity) entity).onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, null, null);
            }
            world.addEntity(entity);
        }
    }

    protected void dataForOre(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.66F) {
            BlockState state = oreTypes.get(fixedRand.nextInt(oreTypes.size())).getDefaultState();
            world.setBlockState(pos, state, 2);
        }
    }

    protected void dataForRune(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.75F) {
            if (Strange.loader.hasModule(Runestones.class)) {
                BlockState state = Runestones.getRunestoneBlock(world, rand.nextInt(Runestones.dests.size()));
                world.setBlockState(pos, state, 2);
            }
        }
    }

    protected void dataForSapling(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.8F) {
            BlockState state = saplingTypes.get(rand.nextInt(saplingTypes.size())).getDefaultState();
            world.setBlockState(pos, state, 2);
        }
    }

    protected void dataForSpawner(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.8F) {
            BlockState state = Blocks.SPAWNER.getDefaultState();
            world.setBlockState(pos, state, 2);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof MobSpawnerTileEntity) {
                ((MobSpawnerTileEntity) tile).getSpawnerBaseLogic().setEntityType(DungeonHooks.getRandomDungeonMob(fixedRand));
            }
        }
    }

    protected void dataForStorage(IWorld world, BlockPos pos, String data, Random rand)
    {
        if (rand.nextFloat() < 0.66F) {
            WoodType woodType = woodTypes.get(fixedRand.nextInt(woodTypes.size()));

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

            BlockState state = types.get(fixedRand.nextInt(types.size()));

            List<ResourceLocation> tables = Arrays.asList(
                LootTables.CHESTS_SHIPWRECK_SUPPLY,
                LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE,
                LootTables.CHESTS_IGLOO_CHEST
            );
            ResourceLocation lootTable = getVanillaLootTableResourceLocation(data, tables.get(rand.nextInt(tables.size())));

            world.setBlockState(pos, state, 2);
            LockableLootTileEntity.setLootTable(world, rand, pos, lootTable);
        }
    }

    public ResourceLocation getVanillaLootTableResourceLocation(String name, ResourceLocation defaultTable)
    {
        ResourceLocation lootTable = defaultTable;

        Set<ResourceLocation> lootTables = LootTables.func_215796_a();
        for (ResourceLocation res : lootTables) {
            String[] s = res.getPath().split("/");
            if (s.length == 0) continue;
            String path = s[s.length-1];
            if (name.contains(path)) {
                lootTable = res;
            }
        }

        return lootTable;
    }
}
