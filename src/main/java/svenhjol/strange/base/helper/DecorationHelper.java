package svenhjol.strange.base.helper;

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.charm.Charm;
import svenhjol.charm.decoration.block.BookshelfChestBlock;
import svenhjol.charm.decoration.module.AllTheBarrels;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.charm.decoration.module.Crates;
import svenhjol.charm.decoration.module.GoldLanterns;
import svenhjol.charm.decoration.tileentity.BookshelfChestTileEntity;
import svenhjol.meson.enums.WoodType;
import svenhjol.meson.helper.LootHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.ruins.module.EntitySpawner;
import svenhjol.strange.ruins.tile.EntitySpawnerTileEntity;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.scrolls.block.WritingDeskBlock;
import svenhjol.strange.scrolls.module.Scrollkeepers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecorationHelper
{
    public static final String ANVIL = "anvil";
    public static final String ARMOR = "armor";
    public static final String BLOCK = "block";
    public static final String BOOKSHELF = "bookshelf";
    public static final String CARPET = "carpet";
    public static final String CAULDRON = "cauldron";
    public static final String CHEST = "chest";
    public static final String DECORATION = "decoration";
    public static final String ENTITY = "entity";
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
        Blocks.ORANGE_CARPET,
        Blocks.RED_CARPET,
        Blocks.YELLOW_CARPET,
        Blocks.LIME_CARPET,
        Blocks.GREEN_CARPET,
        Blocks.CYAN_CARPET,
        Blocks.BLUE_CARPET,
        Blocks.LIGHT_BLUE_CARPET,
        Blocks.PURPLE_CARPET,
        Blocks.MAGENTA_CARPET,
        Blocks.PINK_CARPET,
        Blocks.BLACK_CARPET,
        Blocks.GRAY_CARPET,
        Blocks.LIGHT_GRAY_CARPET,
        Blocks.BROWN_CARPET
    );

    public static List<ResourceLocation> storageLootTables = Arrays.asList(
        LootTables.CHESTS_SHIPWRECK_SUPPLY,
        LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE,
        LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE,
        LootTables.CHESTS_VILLAGE_VILLAGE_SAVANNA_HOUSE,
        LootTables.CHESTS_VILLAGE_VILLAGE_SNOWY_HOUSE,
        LootTables.CHESTS_VILLAGE_VILLAGE_TAIGA_HOUSE,
        LootTables.CHESTS_VILLAGE_VILLAGE_BUTCHER
    );

    public static List<ResourceLocation> chestLootTables = Arrays.asList(
        LootTables.CHESTS_SIMPLE_DUNGEON,
        LootTables.CHESTS_SHIPWRECK_TREASURE
    );

    public static List<Block> decorationTypes = new ArrayList<>(Arrays.asList(
        Blocks.BLAST_FURNACE,
        Blocks.FURNACE,
        Blocks.SMOKER,
        Blocks.CARVED_PUMPKIN,
        Blocks.DISPENSER,
        Blocks.OBSERVER,
        Blocks.LECTERN,
        Blocks.TRAPPED_CHEST,
        Blocks.CAULDRON,
        Blocks.COMPOSTER,
        Blocks.BREWING_STAND,
        Blocks.STONECUTTER,
        Blocks.PUMPKIN,
        Blocks.MELON,
        Blocks.LANTERN,
        Blocks.COBWEB,
        Blocks.HAY_BLOCK,
        Blocks.JUKEBOX,
        Blocks.NOTE_BLOCK,
        Blocks.FLETCHING_TABLE,
        Blocks.SMITHING_TABLE,
        Blocks.CRAFTING_TABLE,
        Blocks.CARTOGRAPHY_TABLE,
        Blocks.ANVIL,
        Blocks.CHIPPED_ANVIL,
        Blocks.DAMAGED_ANVIL,
        Blocks.BELL,
        Blocks.CHEST,
        Blocks.BARREL,
        Blocks.POTTED_CACTUS,
        Blocks.POLISHED_GRANITE,
        Blocks.POLISHED_DIORITE,
        Blocks.POLISHED_ANDESITE,
        Blocks.IRON_BLOCK,
        Blocks.GOLD_BLOCK,
        Blocks.EMERALD_BLOCK,
        Blocks.EMERALD_ORE,
        Blocks.LAPIS_BLOCK,
        Blocks.DIAMOND_ORE,
        Blocks.GOLD_ORE,
        Blocks.WHITE_WOOL,
        Blocks.BLACK_WOOL,
        Blocks.COAL_BLOCK,
        Blocks.BONE_BLOCK,
        Blocks.SLIME_BLOCK
    ));

    public static List<WoodType> woodTypes = new ArrayList<>(Arrays.asList(WoodType.values()));

    static {
        if (Strange.hasModule(Scrollkeepers.class))
            decorationTypes.add(Scrollkeepers.block);
    }

    public static StructureBlockReplacement STRUCTURE_BLOCK_INSTANCE = new StructureBlockReplacement();

    public static class StructureBlockReplacement
    {
        protected String data;
        protected Rotation rotation;
        protected BlockState state;
        protected BlockPos pos;
        protected CompoundNBT nbt;
        protected Random fixedRand;
        protected Random rand;
        protected BlockInfo blockInfo;
        protected float chance;

        public BlockInfo replace(Rotation rotation, BlockInfo blockInfo, String data, Random rand)
        {
            this.fixedRand = rand;
            this.rotation = rotation;
            this.blockInfo = blockInfo;
            this.pos = blockInfo.pos;
            this.state = null;
            this.nbt = null;
            this.rand = new Random(this.pos.toLong());

            if (data.contains("|")) {
                String[] split = data.split("\\|");
                data = split[this.rand.nextInt(split.length)];
            }

            this.data = data.trim();
            this.chance = getChance(this.data, 0.0F);

            if (this.data.startsWith(ANVIL)) anvil();
            if (this.data.startsWith(ARMOR)) armorStand();
            if (this.data.startsWith(BLOCK)) block();
            if (this.data.startsWith(BOOKSHELF)) bookshelf();
            if (this.data.startsWith(CARPET)) carpet();
            if (this.data.startsWith(CAULDRON)) cauldron();
            if (this.data.startsWith(CHEST)) chest();
            if (this.data.startsWith(DECORATION)) decoration();
            if (this.data.startsWith(ENTITY)) entity();
            if (this.data.startsWith(FLOWER)) flower();
            if (this.data.startsWith(LANTERN)) lantern();
            if (this.data.startsWith(LAVA)) lava();
            if (this.data.startsWith(LECTERN)) lectern();
            if (this.data.startsWith(MOB)) mob();
            if (this.data.startsWith(ORE)) ore();
            if (this.data.startsWith(POTTED)) potted();
            if (this.data.startsWith(RUNE)) rune();
            if (this.data.startsWith(SAPLING)) sapling();
            if (this.data.startsWith(SPAWNER)) spawner();
            if (this.data.startsWith(STORAGE)) storage();

            if (this.state == null) {
                this.state = Blocks.AIR.getDefaultState();
            }

            return new BlockInfo(this.pos, this.state, this.nbt);
        }

        protected void anvil()
        {
            float f = rand.nextFloat();
            if (f < 0.33F) {
                this.state = Blocks.ANVIL.getDefaultState();
            } else if (f < 0.66F) {
                this.state = Blocks.CHIPPED_ANVIL.getDefaultState();
            } else if (f < 1.0F) {
                this.state = Blocks.DAMAGED_ANVIL.getDefaultState();
            }
        }

        protected void armorStand()
        {
            EntitySpawnerTileEntity tile = EntitySpawner.tile.create();
            if (tile == null) return;
            nbt = new CompoundNBT();

            tile.entity = EntityType.ARMOR_STAND.getRegistryName();
            tile.meta = this.data;
            tile.rotation = this.rotation;
            tile.write(this.nbt);

            this.state = EntitySpawner.block.getDefaultState();
        }

        protected void block()
        {
            if (!withChance(0.8F)) return;
            String type = getValue("type", this.data, "");
            if (type.isEmpty()) return;

            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(type));
            if (block == null) return;

            this.state = block.getDefaultState();
        }

        protected void bookshelf()
        {
            // TODO variant bookshelves
            if (Charm.hasModule(BookshelfChests.class) && withChance(0.22F)) {
                state = ((Block) BookshelfChests.blocks.get(WoodType.OAK)).getDefaultState()
                    .with(BookshelfChestBlock.SLOTS, 9);

                BookshelfChestTileEntity tile = BookshelfChests.tile.create();
                if (tile != null) {
                    tile.setLootTable(LootTables.CHESTS_STRONGHOLD_LIBRARY, rand.nextLong());
                    this.nbt = new CompoundNBT();
                    tile.write(this.nbt);
                }
            } else {
                state = Blocks.BOOKSHELF.getDefaultState();
            }
        }

        protected void carpet()
        {
            List<Block> types = new ArrayList<>(carpetTypes);
            Collections.shuffle(types, fixedRand);

            int type = getValue("type", this.data, 0);
            if (type > types.size()) type = 0;
            state = types.get(type).getDefaultState();
        }

        protected void cauldron()
        {
            state = Blocks.CAULDRON.getDefaultState()
                .with(CauldronBlock.LEVEL, (int)Math.max(3.0F, 4.0F * rand.nextFloat()));
        }

        protected void chest()
        {
            Block chest = null;

            if (!withChance(0.66F)) return;

            if (StrangeLoader.quarkVariantChests != null
                && StrangeLoader.quarkVariantChests.hasModule()
                && rand.nextFloat() < 0.75F)
                chest = StrangeLoader.quarkVariantChests.getRandomChest(rand);

            if (chest == null)
                chest = Blocks.CHEST;

            state = chest.getDefaultState();
            state = setFacing(state, ChestBlock.FACING, getValue("facing", this.data, "north"));

            ResourceLocation defaultLoot = chestLootTables.get(rand.nextInt(chestLootTables.size()));
            ChestTileEntity tile = TileEntityType.CHEST.create();
            if (tile != null) {
                tile.setLootTable(getLootTable(this.data, defaultLoot), rand.nextLong());
                nbt = new CompoundNBT();
                tile.write(this.nbt);
            }
        }

        protected void decoration()
        {
            if (!withChance(0.85F)) return;

            Direction facing = getFacing(getValue("facing", this.data, "north"));
            Block block = decorationTypes.get(rand.nextInt(decorationTypes.size()));
            state = block.getDefaultState();

            if (block == Blocks.BLAST_FURNACE) state = state.with(BlastFurnaceBlock.FACING, facing);
            if (block == Blocks.FURNACE) state = state.with(FurnaceBlock.FACING, facing);
            if (block == Blocks.SMOKER) state = state.with(SmokerBlock.FACING, facing);
            if (block == Blocks.CARVED_PUMPKIN) state = state.with(CarvedPumpkinBlock.FACING, facing);
            if (block == Blocks.DISPENSER) state = state.with(DispenserBlock.FACING, facing);
            if (block == Blocks.OBSERVER) state = state.with(ObserverBlock.FACING, facing);
            if (block == Blocks.LECTERN) state = state.with(LecternBlock.FACING, facing);
            if (block == Blocks.TRAPPED_CHEST) state = state.with(TrappedChestBlock.FACING, facing);
            if (block == Blocks.CAMPFIRE) state = state.with(CampfireBlock.LIT, false);
            if (block == Blocks.CAULDRON) state = state.with(CauldronBlock.LEVEL, rand.nextInt(3));
            if (block == Blocks.COMPOSTER) state = state.with(ComposterBlock.LEVEL, rand.nextInt(7));
            if (block == Scrollkeepers.block) state = state.with(WritingDeskBlock.FACING, facing);
        }

        protected void entity()
        {
            EntitySpawnerTileEntity tile = EntitySpawner.tile.create();
            if (tile == null) return;
            nbt = new CompoundNBT();

            String type = getValue("type", this.data, "");
            if (type.isEmpty()) return;

            EntityType<?> ent = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(type));
            if (ent == null) return;

            tile.entity = ent.getRegistryName();
            tile.meta = this.data;
            tile.rotation = this.rotation;
            tile.write(this.nbt);

            this.state = EntitySpawner.block.getDefaultState();
        }

        protected void flower()
        {
            if (!withChance(0.8F)) return;
            state = flowerTypes.get(rand.nextInt(flowerTypes.size())).getDefaultState();
        }

        protected void lantern()
        {
            if (!withChance(0.9F)) return;

            state = Blocks.LANTERN.getDefaultState();

            if (Charm.hasModule(GoldLanterns.class) && rand.nextFloat() < 0.25F) {
                state = GoldLanterns.block.getDefaultState();
            }

            if (data.contains("hanging")) {
                state = state.with(LanternBlock.HANGING, true);
            }
        }

        protected void lava()
        {
            state = Blocks.MAGMA_BLOCK.getDefaultState();

            if (fixedRand.nextFloat() < 0.5F) {
                state = Blocks.LAVA.getDefaultState();
            }
        }

        protected void lectern()
        {
            if (!withChance(0.8F)) return;

            state = Blocks.LECTERN.getDefaultState();
            state = setFacing(state, LecternBlock.FACING, getValue("facing", this.data, "north"));
        }

        protected void mob()
        {
            if (!withChance(0.75F)) return;

            EntitySpawnerTileEntity tile = EntitySpawner.tile.create();
            if (tile == null) return;

            String type = getValue("type", this.data, "");
            if (type.isEmpty()) return;
            nbt = new CompoundNBT();

            tile.entity = new ResourceLocation(type);
            tile.health = getValue("health", this.data, 0.0D);
            tile.persist = getValue("persist", this.data, true);
            tile.count = getValue("count", this.data, 1);
            tile.rotation = this.rotation;
            tile.write(this.nbt);

            this.state = EntitySpawner.block.getDefaultState();
        }

        protected void ore()
        {
            if (!withChance(0.8F)) return;

            String type = getValue("type", this.data, "");
            if (!type.isEmpty()) {
                Block ore = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(type));
                if (ore != null) {
                    state = ore.getDefaultState();
                    return;
                }
            }

            state = oreTypes.get(fixedRand.nextInt(oreTypes.size())).getDefaultState();
        }

        protected void potted()
        {
            if (!withChance(0.8F)) return;
            state = pottedTypes.get(rand.nextInt(pottedTypes.size())).getDefaultState();
        }

        protected void rune()
        {
            if (!Strange.hasModule(Runestones.class) || !withChance(0.75F)) {
                state = Blocks.STONE.getDefaultState();
                return;
            }
            String type = getValue("type", this.data, "overworld");

            switch (type) {
                case "nether":
                    state = Runestones.getRandomBlock(DimensionType.THE_NETHER);
                    break;
                case "end":
                    state = Runestones.getRandomBlock(DimensionType.THE_END);
                    break;
                default:
                    state = Runestones.getRandomBlock(DimensionType.OVERWORLD, this.pos);
                    break;
            }
        }

        protected void sapling()
        {
            if (!withChance(0.8F)) return;
            state = saplingTypes.get(rand.nextInt(saplingTypes.size())).getDefaultState();
        }

        protected void spawner()
        {
            if (!withChance(0.8F)) return;
            state = Blocks.SPAWNER.getDefaultState();

            MobSpawnerTileEntity tile = TileEntityType.MOB_SPAWNER.create();

            if (tile != null) {
                tile.getSpawnerBaseLogic().setEntityType(DungeonHooks.getRandomDungeonMob(fixedRand));
                nbt = new CompoundNBT();
                tile.write(this.nbt);
            }
        }

        protected void storage()
        {
            if (!withChance(0.7F)) return;
            LockableLootTileEntity tile;
            WoodType woodType = woodTypes.get(fixedRand.nextInt(woodTypes.size()));

            List<BlockState> barrels = new ArrayList<>(Arrays.asList(
                Blocks.BARREL.getDefaultState().with(BarrelBlock.PROPERTY_FACING, Direction.UP)
            ));
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

            ResourceLocation defaultLoot = storageLootTables.get(rand.nextInt(storageLootTables.size()));

            if (tile != null) {
                tile.setLootTable(getLootTable(this.data, defaultLoot), rand.nextLong());
                this.nbt = new CompoundNBT();
                tile.write(this.nbt);
            }
        }

        public boolean withChance(float chance)
        {
            float f = this.rand.nextFloat();
            return this.chance > 0 ? f < this.chance : f < chance;
        }

        public float getChance(String data, float def)
        {
            int i = getValue("chance", data, 0);
            return i == 0 ? def : ((float)i)/100.0F;
        }
    }

    public static Direction getFacing(String direction)
    {
        if (direction.equals("east")) return Direction.EAST;
        if (direction.equals("south")) return Direction.SOUTH;
        if (direction.equals("west")) return Direction.WEST;
        return Direction.NORTH;
    }

    public static BlockState setFacing(BlockState state, DirectionProperty prop, String direction)
    {
        if (direction.equals("north")) state = state.with(prop, Direction.NORTH);
        if (direction.equals("east")) state = state.with(prop, Direction.EAST);
        if (direction.equals("south")) state = state.with(prop, Direction.SOUTH);
        if (direction.equals("west")) state = state.with(prop, Direction.WEST);
        return state;
    }

    public static boolean getValue(String key, String name, boolean def)
    {
        String val = getValue(key, name, "false");
        return val.isEmpty() ? def : Boolean.parseBoolean(val);
    }

    public static int getValue(String key, String name, int def)
    {
        int i = Integer.parseInt(getValue(key, name, "0"));
        return i == 0 ? def : i;
    }

    public static double getValue(String key, String name, double def)
    {
        double d = Double.parseDouble(getValue(key, name, "0"));
        return d == 0 ? def : d;
    }

    public static String getValue(String key, String data, String def)
    {
        String lookFor = key.endsWith("=") ? key : key + "=";
        if (data.contains(lookFor)) {
            Pattern p = Pattern.compile(lookFor + "([a-zA-Z0-9_:\\-]+)");
            Matcher m = p.matcher(data);
            if (m.find()) return m.group(1);
        }
        return def;
    }

    public static ResourceLocation getLootTable(String data, ResourceLocation def)
    {
        ResourceLocation lootTable = def;
        String loot = getValue("loot", data, "");
        if (!loot.isEmpty()) {
            List<ResourceLocation> tables = LootHelper.getLootTables();
            for (ResourceLocation res : tables) {
                String[] s = res.getPath().split("/");
                if (loot.contains(s[s.length - 1]))
                    lootTable = res;
            }
        }
        return lootTable;
    }
}
