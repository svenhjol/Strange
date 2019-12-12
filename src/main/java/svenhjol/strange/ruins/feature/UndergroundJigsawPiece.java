package svenhjol.strange.ruins.feature;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;

import java.util.List;

public class UndergroundJigsawPiece extends SingleJigsawPiece
{
    public UndergroundJigsawPiece(String location, List<StructureProcessor> processors)
    {
        super(location, processors, JigsawPattern.PlacementBehaviour.RIGID);
    }

    @Override
    protected PlacementSettings createPlacementSettings(Rotation rotationIn, MutableBoundingBox boundsIn)
    {
        PlacementSettings placementsettings = new PlacementSettings();
        placementsettings.setBoundingBox(boundsIn);
        placementsettings.setRotation(rotationIn);
        placementsettings.func_215223_c(true);
        placementsettings.setIgnoreEntities(false);
        placementsettings.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
        this.processors.forEach(placementsettings::addProcessor);
        this.getPlacementBehaviour().getStructureProcessors().forEach(placementsettings::addProcessor);
        return placementsettings;
    }

    public String toString() {
        return "Underground[" + this.location + "]";
    }
}
