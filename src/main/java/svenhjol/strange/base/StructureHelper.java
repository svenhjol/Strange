package svenhjol.strange.base;

import net.minecraft.block.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.DungeonHooks;
import svenhjol.charm.Charm;
import svenhjol.charm.decoration.block.BookshelfChestBlock;
import svenhjol.charm.decoration.module.AllTheBarrels;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.charm.decoration.module.Crates;
import svenhjol.charm.decoration.module.GoldLanterns;
import svenhjol.charm.decoration.tileentity.BookshelfChestTileEntity;
import svenhjol.meson.enums.WoodType;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.block.WritingDeskBlock;
import svenhjol.strange.scrolls.module.Scrollkeepers;

import java.util.*;

public class StructureHelper
{
    public static final String ANVIL = "anvil";
    public static final String ARMOR = "armor";
    public static final String BOOKSHELF = "bookshelf";
    public static final String CARPET = "carpet";
    public static final String CAULDRON = "cauldron";
    public static final String CHEST = "chest";
    public static final String DECORATION = "decoration";
    public static final String FLOWER = "flower";
    public static final String LANTERN = "lantern";
    public static final String LAVA = "lava";
    public static final String LECTERN = "lectern";
    public static final String MOB = "mob";
    public static final String ORE = "ore";
    public static final String POTTED = "potted";
    public static final String RUNE = "rune";
    public static final String SAPLING = "sapling";
    public static final String SPAWNER = "spawner";
    public static final String STORAGE = "storage";

    public static List<Block> flowerTypes = Arrays.asList(
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
        Blocks.DANDELION,
        Blocks.CORNFLOWER,
        Blocks.ALLIUM,
        Blocks.LILY_OF_THE_VALLEY
    );

    public static List<Block> pottedTypes = Arrays.asList(
        Blocks.POTTED_ACACIA_SAPLING,
        Blocks.POTTED_BIRCH_SAPLING,
        Blocks.POTTED_DARK_OAK_SAPLING,
        Blocks.POTTED_JUNGLE_SAPLING,
        Blocks.POTTED_OAK_SAPLING,
        Blocks.POTTED_SPRUCE_SAPLING,
        Blocks.POTTED_CACTUS,
        Blocks.POTTED_POPPY,
        Blocks.POTTED_AZURE_BLUET,
        Blocks.POTTED_WHITE_TULIP,
        Blocks.POTTED_RED_TULIP,
        Blocks.POTTED_PINK_TULIP,
        Blocks.POTTED_ORANGE_TULIP,
        Blocks.POTTED_BLUE_ORCHID,
        Blocks.POTTED_OXEYE_DAISY,
        Blocks.POTTED_WITHER_ROSE,
        Blocks.POTTED_DANDELION,
        Blocks.POTTED_CORNFLOWER,
        Blocks.POTTED_ALLIUM,
        Blocks.POTTED_LILY_OF_THE_VALLEY
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

    public static List<WoodType> woodTypes = new ArrayList<>(Arrays.asList(WoodType.values()));

    public static class StructureBlockReplacement
    {
        protected String data;
        protected Rotation rotation;
        protected BlockState state;
        protected BlockPos pos;
        protected CompoundNBT nbt;
        protected Random fixedRand;
        protected Random rand;

        protected Direction faceNorth;
        protected Direction faceEast;
        protected Direction faceSouth;
        protected Direction faceWest;

        public StructureBlockReplacement(Rotation rotation, Random fixedRand)
        {
            this.fixedRand = fixedRand;
            this.rotation = rotation;
            this.faceNorth = Direction.NORTH;
            this.faceEast = Direction.EAST;
            this.faceSouth = Direction.SOUTH;
            this.faceWest = Direction.WEST;
        }

        public BlockInfo replace(BlockPos pos, String data)
        {
            this.pos = pos;
            this.state = Blocks.AIR.getDefaultState();
            this.nbt = null;
            this.rand = new Random();

            rand.setSeed(pos.toLong());

            if (data.contains("|")) {
                String[] split = this.data.split("\\|");
                data = split[rand.nextInt(split.length)];
            }

            this.data = data;

            if (this.data.contains(ANVIL)) dataForAnvil();
            if (this.data.contains(ARMOR)) dataForArmorStand();
            if (this.data.contains(BOOKSHELF)) dataForBookshelf();
            if (this.data.contains(CARPET)) dataForCarpet();
            if (this.data.contains(CAULDRON)) dataForCauldron();
            if (this.data.contains(CHEST)) dataForChest();
            if (this.data.contains(DECORATION)) dataForDecoration();
            if (this.data.contains(FLOWER)) dataForFlower();
            if (this.data.contains(LANTERN)) dataForLantern();
            if (this.data.contains(LAVA)) dataForLava();
            if (this.data.contains(LECTERN)) dataForLectern();
            if (this.data.contains(MOB)) dataForMob();
            if (this.data.contains(ORE)) dataForOre();
            if (this.data.contains(POTTED)) dataForPotted();
            if (this.data.contains(RUNE)) dataForRune();
            if (this.data.contains(SAPLING)) dataForSapling();
            if (this.data.contains(SPAWNER)) dataForSpawner();
            if (this.data.contains(STORAGE)) dataForStorage();

            return new BlockInfo(this.pos, this.state, this.nbt);
        }

        protected void dataForAnvil()
        {
            float f = rand.nextFloat();
            if (f < 0.25F) {
                this.state = Blocks.ANVIL.getDefaultState();
            } else if (f < 0.5F) {
                this.state = Blocks.CHIPPED_ANVIL.getDefaultState();
            } else if (f < 0.75F) {
                this.state = Blocks.DAMAGED_ANVIL.getDefaultState();
            }
        }

        protected void dataForArmorStand()
        {
//            Direction facing = faceNorth;
//            ArmorStandEntity stand = EntityType.ARMOR_STAND.create(world.getWorld());
//            if (stand == null) return;
//
//            if (data.contains("east")) facing = faceEast;
//            if (data.contains("south")) facing = faceSouth;
//            if (data.contains("west")) facing = faceWest;
//
//            List<Item> ironHeld = new ArrayList<>(Arrays.asList(
//                Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE
//            ));
//            List<Item> goldHeld = new ArrayList<>(Arrays.asList(
//                Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE
//            ));
//            List<Item> diamondHeld = new ArrayList<>(Arrays.asList(
//                Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL
//            ));
//
//            if (data.contains("chain")) {
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ironHeld.get(rand.nextInt(ironHeld.size()))));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
//            }
//            if (data.contains("iron")) {
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ironHeld.get(rand.nextInt(ironHeld.size()))));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.IRON_HELMET));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.IRON_LEGGINGS));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.IRON_BOOTS));
//            }
//            if (data.contains("gold")) {
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(goldHeld.get(rand.nextInt(goldHeld.size()))));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.GOLDEN_HELMET));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.GOLDEN_BOOTS));
//            }
//            if (data.contains("diamond")) {
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(diamondHeld.get(rand.nextInt(diamondHeld.size()))));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.DIAMOND_HELMET));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
//                if (rand.nextFloat() < 0.25F)
//                    stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.DIAMOND_BOOTS));
//            }
//
//            float yaw = facing.getHorizontalAngle();
//            stand.moveToBlockPosAndAngles(pos, yaw, 0.0F);
//            world.addEntity(stand);
        }

        protected void dataForBookshelf()
        {
            // TODO variant bookshelves
            if (Charm.hasModule(BookshelfChests.class) && rand.nextFloat() < 0.22F) {
                state = ((Block) BookshelfChests.blocks.get(WoodType.OAK)).getDefaultState()
                    .with(BookshelfChestBlock.SLOTS, 9);

                BookshelfChestTileEntity tile = BookshelfChests.tile.create();
                if (tile != null) {
                    tile.setLootTable(getVanillaLootTableResourceLocation(data, LootTables.CHESTS_STRONGHOLD_LIBRARY), rand.nextLong());
                    this.nbt = new CompoundNBT();
                    tile.write(this.nbt);
                }
            } else {
                state = Blocks.BOOKSHELF.getDefaultState();
            }
        }

        protected void dataForCarpet()
        {
            List<Block> types = new ArrayList<>(carpetTypes);
            Collections.shuffle(types, fixedRand);
            state = null;

            for (int i = 0; i < types.size(); i++) {
                if (data.contains(String.valueOf(i))) {
                    state = types.get(i).getDefaultState();
                }
            }
        }

        protected void dataForCauldron()
        {
            state = Blocks.CAULDRON.getDefaultState();

            float f = rand.nextFloat();
            if (f > 0.8F) {
                state = state.with(CauldronBlock.LEVEL, 3);
            } else if (f > 0.6F) {
                state = state.with(CauldronBlock.LEVEL, 2);
            } else if (f > 0.4F) {
                state = state.with(CauldronBlock.LEVEL, 1);
            }
        }

        protected void dataForChest()
        {
            if (rand.nextFloat() < 1.0F) {
                state = Blocks.CHEST.getDefaultState();

                ResourceLocation lootTable = LootTables.CHESTS_SIMPLE_DUNGEON;

                Set<ResourceLocation> lootTables = LootTables.func_215796_a();
                for (ResourceLocation res : lootTables) {
                    String[] s = res.getPath().split("/");
                    if (data.contains(s[s.length - 1])) {
                        lootTable = res;
                    }
                }

                if (data.contains("north")) state = state.with(ChestBlock.FACING, faceNorth);
                if (data.contains("east")) state = state.with(ChestBlock.FACING, faceEast);
                if (data.contains("south")) state = state.with(ChestBlock.FACING, faceSouth);
                if (data.contains("west")) state = state.with(ChestBlock.FACING, faceWest);

                ChestTileEntity tile = TileEntityType.CHEST.create();

                if (tile != null) {
                    tile.setLootTable(getVanillaLootTableResourceLocation(data, lootTable), rand.nextLong());
                    nbt = new CompoundNBT();
                    tile.write(this.nbt);
                }
            }
        }

        protected void dataForDecoration()
        {
            if (rand.nextFloat() < 1.0F) {
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

                state = types.get(rand.nextInt(types.size()));
            }
        }

        protected void dataForFlower()
        {
            if (rand.nextFloat() < 0.8F) {
                state = flowerTypes.get(rand.nextInt(flowerTypes.size())).getDefaultState();
            }
        }

        protected void dataForLantern()
        {
            state = Blocks.LANTERN.getDefaultState();

            if (Charm.hasModule(GoldLanterns.class) && rand.nextFloat() < 0.25F) {
                state = GoldLanterns.block.getDefaultState();
            }

            if (data.contains("hanging")) {
                state = state.with(LanternBlock.HANGING, true);
            }
        }

        protected void dataForLava()
        {
            state = Blocks.MAGMA_BLOCK.getDefaultState();

            if (fixedRand.nextFloat() < 0.5F) {
                state = Blocks.LAVA.getDefaultState();
            }
        }

        protected void dataForLectern()
        {
            if (rand.nextFloat() < 0.8F) {
                state = Blocks.LECTERN.getDefaultState();
                if (data.contains("north")) state = state.with(LecternBlock.FACING, faceNorth);
                if (data.contains("east")) state = state.with(LecternBlock.FACING, faceEast);
                if (data.contains("south")) state = state.with(LecternBlock.FACING, faceSouth);
                if (data.contains("west")) state = state.with(LecternBlock.FACING, faceWest);
            }
        }

        protected void dataForMob()
        {
//            if (rand.nextFloat() < 0.8F) {
//                Entity entity;
//                EntityType<?> type = null;
//                boolean persist = data.contains("persist");
//
//                List<ResourceLocation> entities = new ArrayList<>(ForgeRegistries.ENTITIES.getKeys());
//
//                for (ResourceLocation res : entities) {
//                    if (data.contains(res.getPath())) {
//                        type = ForgeRegistries.ENTITIES.getValue(res);
//                    }
//                }
//
//                if (type == null) return;
//                entity = type.create(world.getWorld());
//
//                if (entity == null) return;
//                entity.moveToBlockPosAndAngles(pos, 0.0F, 0.0F);
//
//                if (entity instanceof MobEntity) {
//                    if (persist) ((MobEntity) entity).enablePersistence();
//                    ((MobEntity) entity).onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, null, null);
//                }
//                world.addEntity(entity);
//            }
        }

        protected void dataForOre()
        {
            if (rand.nextFloat() < 0.66F) {
                state = oreTypes.get(fixedRand.nextInt(oreTypes.size())).getDefaultState();
            }
        }

        protected void dataForPotted()
        {
            if (rand.nextFloat() < 0.8F) {
                state = pottedTypes.get(rand.nextInt(pottedTypes.size())).getDefaultState();
            }
        }

        protected void dataForRune()
        {
//            if (rand.nextFloat() < 0.75F) {
//                if (Strange.loader.hasModule(Runestones.class)) {
//                    state = Runestones.getRunestoneBlock(world, rand.nextInt(Runestones.dests.size()));
//                }
//            }
        }

        protected void dataForSapling()
        {
            if (rand.nextFloat() < 0.8F) {
                state = saplingTypes.get(rand.nextInt(saplingTypes.size())).getDefaultState();
            }
        }

        protected void dataForSpawner()
        {
            if (rand.nextFloat() < 0.8F) {
                state = Blocks.SPAWNER.getDefaultState();

                MobSpawnerTileEntity tile = TileEntityType.MOB_SPAWNER.create();

                if (tile != null) {
                    tile.getSpawnerBaseLogic().setEntityType(DungeonHooks.getRandomDungeonMob(fixedRand));
                    nbt = new CompoundNBT();
                    tile.write(this.nbt);
                }
            }
        }

        protected void dataForStorage()
        {
            if (rand.nextFloat() < 1.0F) {
                LockableLootTileEntity tile;
                WoodType woodType = woodTypes.get(fixedRand.nextInt(woodTypes.size()));

                List<BlockState> barrels = Arrays.asList(
                    Blocks.BARREL.getDefaultState().with(BarrelBlock.PROPERTY_FACING, Direction.UP)
                );
                if (Charm.hasModule(AllTheBarrels.class)) {
                    barrels.add(AllTheBarrels.barrels.get(rand.nextInt(AllTheBarrels.barrels.size())).getDefaultState()
                        .with(BarrelBlock.PROPERTY_FACING, Direction.UP));
                }

                List<BlockState> crates = new ArrayList<>();
                if (Charm.hasModule(Crates.class)) {
                    crates.add(Crates.openTypes.get(woodType).getDefaultState());
                    crates.add(Crates.sealedTypes.get(woodType).getDefaultState());
                }

                if (rand.nextFloat() < 0.5F && crates.size() > 0) {
                    this.state = crates.get(rand.nextInt(crates.size()));
                    tile = Crates.tile.create();
                } else {
                    this.state = barrels.get(rand.nextInt(barrels.size()));
                    tile = TileEntityType.BARREL.create();
                }

                List<ResourceLocation> tables = Arrays.asList(
                    LootTables.CHESTS_SHIPWRECK_SUPPLY,
                    LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE,
                    LootTables.CHESTS_IGLOO_CHEST
                );

                ResourceLocation lootTable = getVanillaLootTableResourceLocation(data, tables.get(rand.nextInt(tables.size())));

                if (tile != null) {
                    tile.setLootTable(getVanillaLootTableResourceLocation(data, lootTable), rand.nextLong());
                    nbt = new CompoundNBT();
                    tile.write(this.nbt);
                }
            }
        }

        public ResourceLocation getVanillaLootTableResourceLocation(String name, ResourceLocation defaultTable)
        {
            ResourceLocation lootTable = defaultTable;

            Set<ResourceLocation> lootTables = LootTables.func_215796_a();
            for (ResourceLocation res : lootTables) {
                String[] s = res.getPath().split("/");
                if (s.length == 0) continue;
                String path = s[s.length - 1];
                if (name.contains(path)) {
                    lootTable = res;
                }
            }

            return lootTable;
        }
    }
}
