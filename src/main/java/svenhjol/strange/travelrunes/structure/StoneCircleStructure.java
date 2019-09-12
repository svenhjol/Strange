package svenhjol.strange.travelrunes.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.*;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.travelrunes.module.Runestones;
import svenhjol.strange.travelrunes.module.StoneCircles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class StoneCircleStructure extends ScatteredStructure<StoneCircleConfig>
{
    private static int tries = 64;
    private static final ResourceLocation CORNER1 = new ResourceLocation(Strange.MOD_ID, "stone_circle/corner1");
    private static final ResourceLocation CORRIDOR1 = new ResourceLocation(Strange.MOD_ID, "stone_circle/corridor1");

    public static IStructurePieceType SCP = StoneCirclePiece::new;
    public static IStructurePieceType SCUP = UndergroundPiece::new;

    public StoneCircleStructure(Function<Dynamic<?>, ? extends StoneCircleConfig> config)
    {
        super(config);
    }

    @Override
    public String getStructureName()
    {
        return "Stone_Circle";
    }

    @Override
    public int getSize()
    {
        return 1;
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

            if (rand.nextInt(2) > 0) return false;

            Biome biome = gen.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));
            return gen.hasStructure(biome, StoneCircles.structure);
        }

        return false;
    }

    @Override
    protected int getSeedModifier()
    {
        return 247474720;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return StoneCircleStructure.Start::new;
    }

    public static class Start extends StructureStart
    {
        public Start(Structure<?> structure, int chunkX, int chunkZ, Biome biome, MutableBoundingBox bb, int ref, long seed)
        {
            super(structure, chunkX, chunkZ, biome, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> generator, TemplateManager templates, int chunkX, int chunkZ, Biome biomeIn)
        {
            BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            this.components.add(new StoneCirclePiece(this.rand, pos));

            UndergroundPiece.generateMultiple(this.components, templates, this.rand, pos);
//            this.components.add(new UndergroundPiece(templates, NOOK1, pos));

            this.recalculateStructureSize();
        }
    }

    public static class UndergroundPiece extends TemplateStructurePiece
    {
        private final ResourceLocation templateName;
        private float integrity;
        private Rotation rotation;

        public static void generateMultiple(List<StructurePiece> components, TemplateManager templates, Random rand, BlockPos pos)
        {
            float integrity = 0.94F + (rand.nextFloat() * 0.06F);
            List<StructurePiece> pieces = new ArrayList<>();


            UndergroundPiece centre = new UndergroundPiece(templates, CORNER1, pos, Rotation.NONE, integrity);
            MutableBoundingBox bb = centre.getBoundingBox();

            // generate east
            pieces.add(new UndergroundPiece(templates, CORRIDOR1, pos.offset(Direction.EAST, 9), Rotation.NONE, integrity));
            pieces.add(new UndergroundPiece(templates, CORNER1, pos.offset(Direction.EAST, 20), Rotation.NONE, integrity));

            // generate north
            pieces.add(new UndergroundPiece(templates, CORRIDOR1, pos.offset(Direction.NORTH, 11).offset(Direction.EAST, 8), Rotation.CLOCKWISE_90, integrity));
            pieces.add(new UndergroundPiece(templates, CORNER1, pos.offset(Direction.NORTH, 20).offset(Direction.EAST, 8), Rotation.CLOCKWISE_90, integrity));

            // generate south
            pieces.add(new UndergroundPiece(templates, CORRIDOR1, pos.offset(Direction.SOUTH, 9).offset(Direction.EAST, 8), Rotation.CLOCKWISE_90, integrity));
            pieces.add(new UndergroundPiece(templates, CORRIDOR1, pos.offset(Direction.SOUTH, 20).offset(Direction.EAST, 8), Rotation.CLOCKWISE_90, integrity));
            pieces.add(new UndergroundPiece(templates, CORNER1, pos.offset(Direction.SOUTH, 31).offset(Direction.EAST, 8), Rotation.CLOCKWISE_90, integrity));


            // add all components
            components.add(centre);
            components.addAll(pieces);
        }

        public UndergroundPiece(TemplateManager templates, ResourceLocation template, BlockPos pos, Rotation rotation, float integrity)
        {
            super(SCUP, 0);

            this.templateName = template;
            this.templatePosition = pos;
            this.integrity = integrity;
            this.rotation = rotation;
            this.setup(templates);
        }

        public UndergroundPiece(TemplateManager templates, CompoundNBT tag)
        {
            super(SCUP, tag);

            this.templateName = new ResourceLocation(tag.getString("Template"));
            this.integrity = tag.getFloat("Integrity");
            this.rotation = Rotation.valueOf(tag.getString("Rotation"));
            this.setup(templates);
        }

        private void setup(TemplateManager templates)
        {
            Template template = templates.getTemplateDefaulted(this.templateName);
            PlacementSettings placement = (new PlacementSettings())
                .setRotation(this.rotation)
                .setMirror(Mirror.NONE)
                .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);

            this.setup(template, this.templatePosition, placement);
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
                .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);

            int y = 32;
            this.templatePosition = new BlockPos(this.templatePosition.getX(), y, this.templatePosition.getZ());
//            BlockPos tpos = Template.getTransformedPos(
//                new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1),
//                Mirror.NONE,
//                Rotation.NONE,
//                BlockPos.ZERO
//            ).add(this.templatePosition);
//
//            // TODO recalculate based on block types underground
//            this.templatePosition = tpos;
            return super.addComponentParts(world, rand, bb, chunkPos);
        }

        @Override
        protected void handleDataMarker(String data, BlockPos pos, IWorld world, Random rand, MutableBoundingBox bb)
        {
            if (data.equals("chest")) {
                // TODO do things
                world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
            } else if (data.equals("mob1")) {
                // TODO do things
                IllusionerEntity illusioner = (IllusionerEntity) EntityType.ILLUSIONER.create(world.getWorld());
                illusioner.enablePersistence();
                illusioner.moveToBlockPosAndAngles(pos, 0.0F, 0.0F);
                illusioner.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, null, null);
                world.addEntity(illusioner);
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            }
        }
    }

    public static class StoneCirclePiece extends ScatteredStructurePiece
    {
        public StoneCirclePiece(Random rand, BlockPos pos)
        {
            super(SCP, rand, pos.getX(), 64, pos.getZ(), 16, 6, 16);
        }

        public StoneCirclePiece(TemplateManager templateManager, CompoundNBT tag)
        {
            super(SCP, tag);
        }

        @Override
        public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunkPos)
        {
            int y = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.minX, this.boundingBox.minZ);
            MutableBlockPos pos;
            BlockPos surfacePos, surfacePosDown;
            GenerationConfig config = new GenerationConfig();

            if (world.getDimension().getType() == DimensionType.THE_NETHER) {

                config.runeChance = 0.75F;
                config.radius = rand.nextInt(4) + 5;
                config.columnMinHeight = 3;
                config.columnVariation = 4;
                config.blocks = new ArrayList<>(Arrays.asList(
                    Blocks.NETHER_BRICKS.getDefaultState(),
                    Blocks.RED_NETHER_BRICKS.getDefaultState(),
                    Blocks.COAL_BLOCK.getDefaultState(),
                    Blocks.OBSIDIAN.getDefaultState()
                ));

                for (int i = 20; i < 100; i++) {
                    for (int ii = 1; ii < tries; ii++) {
                        pos = new MutableBlockPos(this.boundingBox.minX, i, this.boundingBox.minZ);
                        surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                        surfacePosDown = surfacePos.down();

                        if (world.isAirBlock(surfacePos)
                            && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.NETHERRACK)
                        ) {
                            return generateCircle(world, pos, rand, config);
                        }
                    }
                }

            } else if (world.getDimension().getType() == DimensionType.THE_END) {

                config.runeChance = 0.9F;
                config.radius = rand.nextInt(7) + 4;
                config.columnMinHeight = 3;
                config.columnVariation = 3;
                config.blocks = new ArrayList<>(Arrays.asList(
                    Blocks.OBSIDIAN.getDefaultState()
                ));

                for (int ii = 1; ii < tries; ii++) {
                    pos = new MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                    surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    surfacePosDown = surfacePos.down();

                    if (world.isAirBlock(surfacePos)
                        && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.END_STONE)
                    ) {
                        return generateCircle(world, pos, rand, config);
                    }
                }

            } else {

                config.runeChance = 0.6F;
                config.radius = rand.nextInt(6) + 5;
                config.columnMinHeight = 2;
                config.columnVariation = 3;
                config.blocks = new ArrayList<>(Arrays.asList(
                    Blocks.STONE.getDefaultState(),
                    Blocks.COBBLESTONE.getDefaultState(),
                    Blocks.MOSSY_COBBLESTONE.getDefaultState()
                ));

                for (int ii = 1; ii < tries; ii++) {
                    pos = new MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                    surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    surfacePosDown = surfacePos.down();

                    if ((world.isAirBlock(surfacePos) || world.hasWater(surfacePos))
                        && world.getBlockState(surfacePosDown).isSolid() && world.isSkyLightMax(surfacePosDown)
                    ) {
                        return generateCircle(world, pos, rand, config);
                    }
                }
            }

            return false;
        }

        public boolean generateCircle(IWorld world, MutableBlockPos pos, Random rand, GenerationConfig config)
        {
            boolean generated = false;
            boolean generatedWithRune = false;
            boolean runestonesEnabled = Strange.loader.hasModule(Runestones.class);

            List<Integer> availableRunes = new ArrayList<>();
            if (runestonesEnabled) {
                for (int i = 0; i < Runestones.destinations.size(); i++) {
                    availableRunes.add(i);
                }
            }

            if (config.blocks.isEmpty()) {
                Meson.warn("You must pass blockstates to generate a circle");
                return false;
            }

            for (int i = 0; i < 360; i += 45)
            {
                double x1 = config.radius * Math.cos(i * Math.PI / 180);
                double z1 = config.radius * Math.sin(i * Math.PI / 180);

                for (int k = 5; k > -15; k--) {
                    BlockPos findPos = pos.add(x1, k, z1);
                    BlockPos findPosUp = findPos.up();
                    BlockState findState = world.getBlockState(findPos);
                    BlockState findStateUp = world.getBlockState(findPosUp);

                    if ((findState.isSolid() || findState.getBlock() == Blocks.LAVA)
                        && (findStateUp.isAir(world, findPosUp) || world.hasWater(findPosUp))
                    ) {
                        boolean madeColumn = false;

                        int maxHeight = rand.nextInt(config.columnVariation + 1) + config.columnMinHeight;
                        world.setBlockState(findPos, config.blocks.get(0), 2);

                        for (int l = 1; l < maxHeight; l++) {
                            float f = rand.nextFloat();
                            BlockState state = config.blocks.get(rand.nextInt(config.blocks.size()));

                            if (runestonesEnabled && l == maxHeight - 1 && f < config.runeChance) {
                                int index = rand.nextInt(availableRunes.size());
                                int rune = availableRunes.get(index);
                                availableRunes.remove(index);

                                state = Runestones.getRunestoneBlock(world, rune);
                                generatedWithRune = true;
                            }

                            world.setBlockState(findPos.up(l), state, 2);
                            madeColumn = true;
                        }

                        if (madeColumn) {
                            generated = true;
                            break;
                        }
                    }
                }
            }

            if (generatedWithRune) {
                Meson.log("Generated with rune " + pos);
            }

            return generated;
        }
    }

    public static class GenerationConfig
    {
        public int radius = 4;
        public float runeChance = 0.5F;
        public int columnMinHeight = 3;
        public int columnVariation = 3;
        public List<BlockState> blocks = new ArrayList<>();
    }
}
