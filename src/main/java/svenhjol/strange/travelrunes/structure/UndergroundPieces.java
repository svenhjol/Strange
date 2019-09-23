package svenhjol.strange.travelrunes.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static svenhjol.strange.travelrunes.structure.UndergroundPieces.PieceType.*;
import static svenhjol.strange.travelrunes.structure.UndergroundStructure.SCUP;

public class UndergroundPieces
{
    public enum PieceType
    {
        Corridor,
        Junction,
        Large
    }

    static Map<PieceType, int[]> sizes = new HashMap<>();
    static {
        sizes.put(Corridor, new int[]{5, 8, 11});
        sizes.put(Junction, new int[]{9, 8, 9});
        sizes.put(Large, new int[]{17, 15, 17});
    }

    public static class UndergroundPiece extends TemplateStructurePiece
    {
        private final ResourceLocation templateName;
        private float integrity;
        private Rotation rotation;
        public int x;
        public int y;
        public int z;

        public UndergroundPiece(TemplateManager templates, ResourceLocation template, BlockPos pos, Rotation rotation)
        {
            super(SCUP, 0);

            this.templateName = template;
            this.templatePosition = pos;
            this.integrity = 1.0F;
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

            BlockPos size = template.getSize();
            this.x = size.getX();
            this.y = size.getY();
            this.z = size.getZ();

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
            BlockState state = Blocks.AIR.getDefaultState();

            if (data.equals("chest1") || data.equals("chest2") || data.equals("chest3") || data.equals("chest4")) {
                // TODO do things
                state = Blocks.CHEST.getDefaultState();

            } else if (data.equals("mob1")) {
                // TODO do things
                IllusionerEntity illusioner = (IllusionerEntity) EntityType.ILLUSIONER.create(world.getWorld());
                illusioner.enablePersistence();
                illusioner.moveToBlockPosAndAngles(pos, 0.0F, 0.0F);
                illusioner.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, null, null);
                world.addEntity(illusioner);

            } else if (data.equals("erosion")) {
                if (rand.nextFloat() > 0.2F) {
                    state = Blocks.STONE_BRICKS.getDefaultState();
                }

            } else if (data.equals("skull1")) {
                float f = rand.nextFloat();
                if (f < 0.05F) {
                    state = Blocks.WITHER_SKELETON_SKULL.getDefaultState();
                } else if (f < 0.1F) {
                    state = Blocks.ZOMBIE_HEAD.getDefaultState();
                } else if (f < 0.5F) {
                    state = Blocks.SKELETON_SKULL.getDefaultState();
                }

            } else if (data.equals("lantern_hanging")) {
                float f = rand.nextFloat();
                if (f < 0.3F) {
                    state = Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true);
                }
            }

            world.setBlockState(pos, state, 2);
        }
    }
}
