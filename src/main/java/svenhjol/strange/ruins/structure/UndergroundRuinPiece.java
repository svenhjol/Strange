package svenhjol.strange.ruins.structure;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.base.StrangeTemplateStructurePiece;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static svenhjol.strange.ruins.structure.UndergroundRuinStructure.UNDERGROUND_RUIN_PIECE;

public class UndergroundRuinPiece extends StrangeTemplateStructurePiece
{
    public int depth = 0;
    public int verticalRange = 20;

    public UndergroundRuinPiece(TemplateManager templates, ResourceLocation template, BlockPos pos, Rotation rotation, int depth)
    {
        super(UNDERGROUND_RUIN_PIECE, 0);

        this.templateName = template;
        this.templatePosition = pos;
        this.integrity = 1.0F;
        this.depth = depth;
        this.rotation = rotation;

        this.setup(templates);
    }

    public UndergroundRuinPiece(TemplateManager templates, CompoundNBT tag)
    {
        super(UNDERGROUND_RUIN_PIECE, tag);

        this.setup(templates);
    }

    @Override
    public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunkPos)
    {
        BlockPos originalPos = new BlockPos(this.templatePosition.getX(), this.templatePosition.getY(), this.templatePosition.getZ());
        BlockPos foundPos = null;

        final BlockPos templateSize = this.template.getSize();

        if (false) {
            List<BlockPos> valid = BlockPos.getAllInBox(originalPos.add(0, -verticalRange, 0), originalPos.add(0, verticalRange, 0))
                .map(BlockPos::toImmutable)
                .filter(p -> (world.isAirBlock(p) || world.getBlockState(p).getMaterial() == Material.WATER)
                    && world.getBlockState(p.add(1, templateSize.getY() - 1, 1)).isSolid()
                    && world.getBlockState(p.add(templateSize.getX() - 1, templateSize.getY() - 1, templateSize.getZ() - 1)).isSolid()
                )
                .collect(Collectors.toList());

            if (!valid.isEmpty()) {
                for (int i = world.getSeaLevel() - 12; i > 12; i--) {
                    foundPos = new BlockPos(originalPos.getX(), i - templateSize.getY(), originalPos.getZ());
                    if (world.getBlockState(foundPos).isSolid()) {
                        foundPos = new BlockPos(originalPos.getX() + templateSize.getX(), i - templateSize.getY(), originalPos.getZ() + templateSize.getZ());
                        if (world.getBlockState(foundPos).isSolid()) {
                            foundPos = foundPos.down(5);
                            break;
                        }
                    }
                }
            }

            if (foundPos != null && foundPos.down(templateSize.getY()).getY() > 2) {
                this.templatePosition = new BlockPos(this.templatePosition.getX(), foundPos.getY(), this.templatePosition.getZ());
            }
        }

        // offset the template down according to depth
        if (depth > 0) {
            this.templatePosition = new BlockPos(this.templatePosition.getX(), this.templatePosition.getY() - depth, this.templatePosition.getZ());
        }

        // don't let the template render out of the bottom of the world
        int yo = this.templatePosition.getY() - templateSize.getY();
        if (yo < 2) {
            Meson.debug("Structure too deep (would be " + yo + "), moving up", this.templatePosition);
            this.templatePosition = new BlockPos(this.templatePosition.getX(), 2 + templateSize.getY(), this.templatePosition.getZ());
        }

        return super.addComponentParts(world, rand, bb, chunkPos);
    }

}