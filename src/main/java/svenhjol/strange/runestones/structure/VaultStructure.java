package svenhjol.strange.runestones.structure;

import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.module.StoneCircles;
import svenhjol.strange.runestones.structure.VaultPieces.VaultPieceType;
import svenhjol.strange.runestones.structure.VaultPieces.VaultPiece;

import javax.annotation.Nullable;
import java.util.*;

import static svenhjol.strange.runestones.structure.VaultPieces.VaultPieceType.*;

public class VaultStructure
{
    public static IStructurePieceType VAULT_PIECE = VaultPiece::new;
    public static Map<VaultPieceType, List<ResourceLocation>> pieceTypes = new HashMap<>();

    public static final int CORRIDOR_X = 5;
    public static final int CORRIDOR_Z = 11;
    public static final int JUNCTION_X = 9;
    public static final int JUNCTION_Z = 9;
    public static final int LARGE_X = 17;
    public static final int LARGE_Z = 17;

    public int maxIterations = StoneCircles.vaultSize;
    public Biome biome;
    public Random rand;
    public TemplateManager templates;
    public List<StructurePiece> components;

    public VaultStructure(TemplateManager templates, List<StructurePiece> components, Biome biome, Random rand)
    {
        this.biome = biome;
        this.rand = rand;
        this.components = components;
        this.templates = templates;

        pieceTypes.put(Junction, new ArrayList<>(Arrays.asList(
            new ResourceLocation(Strange.MOD_ID, "vaults/junction1"),
            new ResourceLocation(Strange.MOD_ID, "vaults/junction2"),
            new ResourceLocation(Strange.MOD_ID, "vaults/junction3"),
            new ResourceLocation(Strange.MOD_ID, "vaults/junction4"),
            new ResourceLocation(Strange.MOD_ID, "vaults/junction5"),
            new ResourceLocation(Strange.MOD_ID, "vaults/junction6")
        )));
        pieceTypes.put(Corridor, new ArrayList<>(Arrays.asList(
            new ResourceLocation(Strange.MOD_ID, "vaults/corridor1"),
            new ResourceLocation(Strange.MOD_ID, "vaults/corridor2"),
            new ResourceLocation(Strange.MOD_ID, "vaults/corridor3"),
            new ResourceLocation(Strange.MOD_ID, "vaults/corridor4"),
            new ResourceLocation(Strange.MOD_ID, "vaults/corridor5"),
            new ResourceLocation(Strange.MOD_ID, "vaults/corridor6")
        )));
        pieceTypes.put(Large, new ArrayList<>(Arrays.asList(
            new ResourceLocation(Strange.MOD_ID, "vaults/large1"),
            new ResourceLocation(Strange.MOD_ID, "vaults/large2"),
            new ResourceLocation(Strange.MOD_ID, "vaults/large3"),
            new ResourceLocation(Strange.MOD_ID, "vaults/large4"),
            new ResourceLocation(Strange.MOD_ID, "vaults/large5"),
            new ResourceLocation(Strange.MOD_ID, "vaults/large6")
        )));
    }

    public void generate(BlockPos startPos)
    {
        if (startPos.getY() == 0 || startPos.getY() > 48) {
            startPos = new BlockPos(startPos.getX(), this.rand.nextInt(12) + 24, startPos.getZ());
        }

        generate(Junction, startPos, 0, Direction.byHorizontalIndex(rand.nextInt(4) + 2), Rotation.NONE);
    }

    public ResourceLocation getRandomTemplate(VaultPieceType type)
    {
        List<ResourceLocation> templates = pieceTypes.get(type);
        return templates.get(this.rand.nextInt(templates.size()));
    }

    public void generate(VaultPieceType pieceType, BlockPos pos, int iterations, @Nullable Direction from, @Nullable Rotation rotation)
    {
        iterations++;

        if (rotation == null) rotation = Rotation.NONE;
        VaultPieces.VaultPiece centre = new VaultPiece(templates, getRandomTemplate(pieceType), pos, rotation);

        components.add(centre);

        for (Direction direction : Direction.values()) {
            if (from != null && direction == from.getOpposite()) continue; // don't generate in the direction came from
            float chance = (1.9F / (float)iterations) * (direction == from ? 0.95F : 0.7F);
            float f = rand.nextFloat();
            if (f > chance || iterations >= maxIterations) continue;

            int nextX, nextZ;
            VaultPieceType nextType;
            BlockPos nextPos = null;

            if (f < 0.12F) {
                nextX = LARGE_X;
                nextZ = LARGE_Z;
                nextType = Large;
            } else {
                nextX = JUNCTION_X;
                nextZ = JUNCTION_Z;
                nextType = Junction;
            }

            if (direction == Direction.NORTH) {
                makeCorridor(pos.add((centre.x - CORRIDOR_X) / 2, 0, -CORRIDOR_Z), Rotation.NONE);
                nextPos = pos.add(-((nextX - centre.x) / 2), 0, -(CORRIDOR_Z + nextZ));
            } else if (direction == Direction.SOUTH) {
                makeCorridor(pos.add((centre.x - CORRIDOR_X) / 2, 0, centre.z), Rotation.NONE);
                nextPos = pos.add(-((nextX - centre.x) / 2), 0, centre.z + CORRIDOR_Z);
            } else if (direction == Direction.EAST) {
                makeCorridor(pos.add(centre.x, 0, ((centre.z - CORRIDOR_X) / 2) + 4), Rotation.COUNTERCLOCKWISE_90);
                nextPos = pos.add(CORRIDOR_Z + centre.x, 0, -((nextZ - centre.x) / 2));
            }
//            } else if (f < 0.4F && direction == Direction.WEST) {
//                makeCorridor(pos.add(-CORRIDOR_Z, 0, ((centre.z - CORRIDOR_X) / 2) + 4), Rotation.COUNTERCLOCKWISE_90);
//                nextPos = pos.add(-CORRIDOR_Z - nextX, 0, -((nextZ - centre.x) / 2));
//            }

            if (nextPos != null) {
                if (nextType == Large && rand.nextFloat() < 0.35F) {
                    nextPos = nextPos.add(0, -7, 0);
                }
                generate(nextType, nextPos, iterations, direction, Rotation.NONE);
            }
        }
    }

    public void makeCorridor(BlockPos pos, Rotation rotation)
    {
        VaultPiece corridor = new VaultPiece(templates, getRandomTemplate(Corridor), pos, rotation);
        components.add(corridor);
    }
}
