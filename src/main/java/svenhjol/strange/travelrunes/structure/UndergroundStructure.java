package svenhjol.strange.travelrunes.structure;

import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.strange.Strange;
import svenhjol.strange.travelrunes.structure.UndergroundPieces.PieceType;
import svenhjol.strange.travelrunes.structure.UndergroundPieces.UndergroundPiece;

import javax.annotation.Nullable;
import java.util.*;

import static svenhjol.strange.travelrunes.structure.UndergroundPieces.PieceType.*;

public class UndergroundStructure
{
    public static IStructurePieceType SCUP = UndergroundPiece::new;
    public static Map<PieceType, List<ResourceLocation>> pieceTypes = new HashMap<>();

    private static final ResourceLocation LIBRARY = new ResourceLocation(Strange.MOD_ID, "stone_circle/library1");
    private static final ResourceLocation CORRIDOR = new ResourceLocation(Strange.MOD_ID, "stone_circle/corridor1");
    private static final ResourceLocation CRYPT = new ResourceLocation(Strange.MOD_ID, "stone_circle/crypt1");

    public static final int CORRIDOR_X = 5;
    public static final int CORRIDOR_Z = 11;
    public static final int JUNCTION_X = 9;
    public static final int JUNCTION_Z = 9;
    public static final int LARGE_X = 17;
    public static final int LARGE_Z = 17;

    public int maxDepth = 6;
    public Biome biome;
    public Random rand;
    public TemplateManager templates;
    public List<StructurePiece> components;

    public UndergroundStructure(TemplateManager templates, List<StructurePiece> components, Biome biome, Random rand)
    {
        this.biome = biome;
        this.rand = rand;
        this.components = components;
        this.templates = templates;

        pieceTypes.put(Junction, new ArrayList<>(Arrays.asList(LIBRARY)));
        pieceTypes.put(Corridor, new ArrayList<>(Arrays.asList(CORRIDOR)));
        pieceTypes.put(Large, new ArrayList<>(Arrays.asList(CRYPT)));
    }

    public void generate(BlockPos startPos)
    {
        UndergroundPiece centre = new UndergroundPiece(templates, getRandomTemplate(Junction), startPos, Rotation.NONE);

        if (startPos.getY() == 0 || startPos.getY() > 32) {
            startPos = new BlockPos(startPos.getX(), 32, startPos.getZ());
        }

        generate(centre, startPos, 0, Direction.byHorizontalIndex(rand.nextInt(4) + 2));
    }

    public ResourceLocation getRandomTemplate(PieceType type)
    {
        List<ResourceLocation> templates = pieceTypes.get(type);
        return templates.get(this.rand.nextInt(templates.size()));
    }

    public void generate(UndergroundPiece centre, BlockPos pos, int depth, @Nullable Direction from)
    {
        components.add(centre);
        depth++;

        for (Direction direction : Direction.values()) {
            if (from != null && direction == from.getOpposite()) continue; // don't generate in the direction came from
            float chance = (1 / (float)depth) * (direction == from ? 0.95F : 0.7F);
            float f = rand.nextFloat();
//            float chance = 1.0F;
            if (f > chance || depth >= maxDepth) continue;

            int nextX, nextZ;
            PieceType nextType;
            BlockPos nextPos = null;

            if (f < 0.14F) {
                nextX = LARGE_X;
                nextZ = LARGE_Z;
                nextType = Large;
            } else {
                nextX = JUNCTION_X;
                nextZ = JUNCTION_Z;
                nextType = Junction;
            }

            if (direction == Direction.NORTH) {
                makeCorridor(pos.add((centre.x - CORRIDOR_X) / 2, 0, -CORRIDOR_Z), Rotation.NONE); // 2, 0, -11
//                pos = pos.add(-((nextX - CORRIDOR_X) / 2), 0, -nextZ); // -2, 0, -9
                nextPos = pos.add(-((nextX - centre.x) / 2), 0, -(CORRIDOR_Z + nextZ));
                // LARGE: pos = pos.add(-6, 0, -17)
            } else if (direction == Direction.SOUTH) {
//                pos = pos.add((centre.x - CORRIDOR_X) / 2, 0, centre.z); // 2, 0, 9
                makeCorridor(pos.add((centre.x - CORRIDOR_X) / 2, 0, centre.z), Rotation.NONE);
//                pos = pos.add(-((nextX - CORRIDOR_X) / 2), 0, CORRIDOR_Z); // -2, 0, 11
                nextPos = pos.add(-((nextX - centre.x) / 2), 0, centre.z + CORRIDOR_Z);
            } else if (direction == Direction.EAST) {
//                pos = pos.add(centre.x, 0, (centre.z - CORRIDOR_X) + 2); // 9, 0, 6
                makeCorridor(pos.add(centre.x, 0, ((centre.z - CORRIDOR_X) / 2) + 4), Rotation.COUNTERCLOCKWISE_90);
//                pos = pos.add(CORRIDOR_Z, 0, -((nextZ - CORRIDOR_X) + 2)); //11, 0, -6
                nextPos = pos.add(CORRIDOR_Z + centre.x, 0, -((nextZ - centre.x) / 2));
            }
//            } else if (direction == Direction.WEST) {
////                pos = pos.add(-CORRIDOR_Z, 0, (centre.z - CORRIDOR_X) + 2); // -11, 0, 6
//                makeCorridor(pos.add(-CORRIDOR_Z, 0, (centre.z - CORRIDOR_X) + 2), Rotation.COUNTERCLOCKWISE_90);
//                nextPos = pos.add(-CORRIDOR_Z + nextX, 0, centre.z - nextZ); // -9, 0, -6
//            }

            if (nextPos != null) {
                UndergroundPiece next = new UndergroundPiece(templates, getRandomTemplate(nextType), nextPos, Rotation.NONE);
                generate(next, nextPos, depth, direction);
            }
        }

    }

    public void makeCorridor(BlockPos pos, Rotation rotation)
    {
        UndergroundPiece corridor = new UndergroundPiece(templates, getRandomTemplate(Corridor), pos, rotation);
        components.add(corridor);
    }
}
