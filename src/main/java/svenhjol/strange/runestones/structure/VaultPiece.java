package svenhjol.strange.runestones.structure;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.base.helper.StructureHelper;

import java.util.Random;

public class VaultPiece extends AbstractVillagePiece
{
    public static IStructurePieceType PIECE = VaultPiece::new;

    protected TemplateManager templates;

    public VaultPiece(TemplateManager templates, JigsawPiece piece, BlockPos pos, int groundLevelDelta, Rotation rotation, MutableBoundingBox bounds) {
        super(PIECE, templates, piece, pos, groundLevelDelta, rotation, bounds);
        this.templates = templates;
    }

    public VaultPiece(TemplateManager templates, CompoundNBT nbt)
    {
        super(templates, nbt, PIECE);
        this.templates = templates;
    }

    @Override
    public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox structureBox, ChunkPos chunk)
    {
        boolean result;
        MutableBoundingBox box = this.jigsawPiece.getBoundingBox(this.templates, pos, this.rotation);

        if (WorldHelper.getBiomeAtPos(world.getWorld(), this.pos).getCategory() == Biome.Category.OCEAN) {
            this.pos = StructureHelper.adjustForOceanFloor(world, this.pos, box);
        }

        try {
            result = super.addComponentParts(world, rand, structureBox, chunk);
        } catch (NullPointerException e) {
            Meson.warn("NullPointer when generating piece, FIXME", e);
            result = false;
        }

        return result;
    }
}
