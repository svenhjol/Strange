package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.Charm;
import svenhjol.charm.helper.ItemHelper;
import svenhjol.charm.module.variant_bookshelves.VariantBookshelves;
import svenhjol.strange.Strange;
import svenhjol.strange.module.structures.Processors;
import svenhjol.strange.module.structures.Structures;

import java.util.*;

public class DecorationReplacementProcessor extends StructureProcessor {
    public static final Codec<DecorationReplacementProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("block").orElse("").forGetter(p -> p.block),
        Codec.STRING.fieldOf("type").orElse("").forGetter(p -> p.decoration),
        Codec.FLOAT.fieldOf("chance").orElse(1.0F).forGetter(p -> p.chance),
        Codec.BOOL.fieldOf("remove").orElse(true).forGetter(p -> p.remove)
    ).apply(instance, DecorationReplacementProcessor::new));

    private final String block;
    private final String decoration;
    private final float chance;
    private final boolean remove;

    public DecorationReplacementProcessor(String block, String decoration, float chance, boolean remove) {
        this.block = block;
        this.decoration = decoration;
        this.chance = chance;
        this.remove = remove;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo NONONONO, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockPos pos = structureBlockInfo2.pos;
        BlockState state = structureBlockInfo2.state;
        CompoundTag nbt = structureBlockInfo2.nbt;
        StructureBlockInfo air = new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null);
        Optional<Direction> prop;

        if (!moduleCheck()) {
            return structureBlockInfo2;
        }

        Optional<Block> block = Registry.BLOCK.getOptional(new ResourceLocation(this.block));
        if (block.isEmpty() || !(state.getBlock() == block.get())) {
            return structureBlockInfo2;
        }

        if (random.nextFloat() > chance) {
            return remove ? air : structureBlockInfo2;
        }

        DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;
        Direction facing = null;

        prop = state.getOptionalValue(facingProp);
        if (prop.isPresent()) {
            facing = prop.get();
        }

        // try and read the loot table
        ResourceLocation decorationId = new ResourceLocation(Strange.MOD_ID, "decorations/" + decoration);
        List<ItemStack> list = new ArrayList<>(Structures.DECORATIONS.getOrDefault(decorationId, List.of()));

        if (!list.isEmpty()) {
            Collections.shuffle(list, random);
            ItemStack stack = list.get(0);
            BlockState newState = ItemHelper.getBlockStateFromItemStack(stack);

            // try and set the block entity tag if present
            if (stack.hasTag()) {
                // TODO: make BlockItem.BLOCK_ENTITY_TAG public
                CompoundTag blockEntityTag = Objects.requireNonNull(stack.getTag()).getCompound("BlockEntityTag");
                if (!blockEntityTag.isEmpty()) {
                    nbt.merge(blockEntityTag);
                }
            }

            // try and set facing property if present
            prop = newState.getOptionalValue(facingProp);
            if (prop.isPresent() && facing != null) {
                newState = newState.setValue(facingProp, facing);
            }

            return new StructureBlockInfo(pos, newState, nbt);
        }

        return remove ? air : structureBlockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.DECORATION_REPLACEMENT;
    }

    protected boolean moduleCheck() {
        if (this.block.equals("bookshelf") && Charm.LOADER.isEnabled(VariantBookshelves.class)) return false;

        return true;
    }
}
