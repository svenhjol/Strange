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
import svenhjol.strange.base.StrangeTemplateStructurePiece;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static svenhjol.strange.ruins.structure.UndergroundRuinStructure.UNDERGROUND_RUIN_PIECE;

public class UndergroundRuinPiece extends StrangeTemplateStructurePiece
{
    public boolean tryFindOpening = false;
    public int verticalRange = 20;

    public UndergroundRuinPiece(TemplateManager templates, ResourceLocation template, BlockPos pos, Rotation rotation, boolean tryFindOpening)
    {
        super(UNDERGROUND_RUIN_PIECE, 0);

        this.templateName = template;
        this.templatePosition = pos;
        this.integrity = 1.0F;
        this.rotation = rotation;
        this.tryFindOpening = tryFindOpening;
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
        if (this.tryFindOpening) {
            BlockPos oPos = new BlockPos(this.templatePosition.getX(), this.templatePosition.getY(), this.templatePosition.getZ());
            BlockPos nPos = null;

            final BlockPos templateSize = this.template.getSize();

            List<BlockPos> valid = BlockPos.getAllInBox(oPos.add(0, -verticalRange, 0), oPos.add(0, verticalRange, 0))
                .map(BlockPos::toImmutable)
                .filter(p -> (world.isAirBlock(p) || world.getBlockState(p).getMaterial() == Material.WATER)
                    && world.getBlockState(p.add(0, templateSize.getY() - 1, 0)).isSolid()
                    && world.getBlockState(p.add(templateSize.getX(), templateSize.getY() - 1, templateSize.getZ())).isSolid()
                )
                .collect(Collectors.toList());

            if (!valid.isEmpty()) {
                for (int i = world.getSeaLevel() - 12; i > 12; i--) {
                    nPos = new BlockPos(oPos.getX(), i - templateSize.getY(), oPos.getZ());
                    if (world.getBlockState(nPos).isSolid()) {
                        nPos = new BlockPos(oPos.getX() + templateSize.getX(), i - templateSize.getY(), oPos.getZ() + templateSize.getZ());
                        if (world.getBlockState(nPos).isSolid()) {
                            nPos = nPos.down(2);
                            break;
                        }
                    }
                }
            }

            if (nPos != null && nPos.down(templateSize.getY()).getY() > 5) {
                this.templatePosition = new BlockPos(this.templatePosition.getX(), nPos.getY(), this.templatePosition.getZ());
            }
        }

        return super.addComponentParts(world, rand, bb, chunkPos);
    }

}