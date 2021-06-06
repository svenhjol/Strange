package svenhjol.strange.module.astrolabes;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmBlockItem;
import svenhjol.charm.block.ICharmBlock;
import svenhjol.charm.helper.DimensionHelper;

import java.util.Optional;

public class AstrolabeBlockItem extends CharmBlockItem {
    public static final String DIMENSION_NBT = "Dimension";
    public static final String POSITION_NBT = "Position";

    public AstrolabeBlockItem(ICharmBlock block) {
        super(block, new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == Astrolabes.ASTROLABE) {
            // trying to link to another astrolabe
            AstrolabeBlockItem.setPosition(stack, pos);
            AstrolabeBlockItem.setDimension(stack, world.dimension());

            if (!world.isClientSide) {
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                Astrolabes.triggerLinkedAstrolabe((ServerPlayer) context.getPlayer());
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return super.useOn(context);
    }

    public static Optional<ResourceKey<Level>> getDimension(ItemStack astrolabe) {
        if (!astrolabe.getOrCreateTag().contains(DIMENSION_NBT))
            return Optional.empty();

        Tag dimension = astrolabe.getOrCreateTag().get(DIMENSION_NBT);
        return DimensionHelper.decodeDimension(dimension);
    }

    public static Optional<BlockPos> getPosition(ItemStack astrolabe) {
        if (!astrolabe.getOrCreateTag().contains(POSITION_NBT))
            return Optional.empty();

        long position = astrolabe.getOrCreateTag().getLong(POSITION_NBT);
        return Optional.of(BlockPos.of(position));
    }

    public static void setDimension(ItemStack astrolabe, ResourceKey<Level> worldKey) {
        DimensionHelper.encodeDimension(worldKey, el
            -> astrolabe.getOrCreateTag().put(DIMENSION_NBT, el));
    }

    public static void setPosition(ItemStack astrolabe, BlockPos position) {
        astrolabe.getOrCreateTag().putLong(POSITION_NBT, position.asLong());
    }
}
