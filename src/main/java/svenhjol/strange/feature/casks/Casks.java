package svenhjol.strange.feature.casks;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charmony.annotation.Configurable;
import svenhjol.charmony.common.CommonFeature;

import java.util.List;
import java.util.function.Supplier;

public class Casks extends CommonFeature {
    static final String BLOCK_ID = "cask";
    static final String STORED_POTIONS_TAG = "stored_potions";
    static Supplier<CaskBlock> block;
    static Supplier<CaskBlock.BlockItem> blockItem;
    static Supplier<BlockEntityType<CaskBlockEntity>> blockEntity;

    @Configurable(name = "Maximum bottles", description = "Maximum number of bottles a cask can hold.")
    public static int maxPortions = 64;

    @Configurable(name = "Preserve contents", description = "If true, a cask remembers it contents when broken.")
    public static boolean preserveContents = true;

    @Override
    public void register() {
        var registry = mod().registry();

        // TODO: CharomyBlockWithEntity no longer needs feature passing
        block = registry.block(BLOCK_ID, () -> new CaskBlock(this));
        blockItem = registry.item(BLOCK_ID, () -> new CaskBlock.BlockItem(block));
        blockEntity = registry.blockEntity(BLOCK_ID, () -> CaskBlockEntity::new, List.of(block));

        // Casks can be burned in a furnace
        registry.fuel(block);

        // Register network packets
        CasksNetwork.register(registry);
    }

    @Override
    public void runWhenEnabled() {
//        BlockBreakEvent.INSTANCE.handle(this::handleBlockBreak);
    }

//    private boolean handleBlockBreak(Level level, BlockPos pos, BlockState state, Player player) {
//        if (level.getBlockEntity(pos) instanceof CaskBlockEntity cask) {
//            var out = new ItemStack(block.get());
//
//            if (preserveContents && cask.portions > 0) {
//                var tag = new CompoundTag();
//                cask.saveAdditional(tag);
//                out.getOrCreateTag().put(STORED_POTIONS_TAG, tag);
//            }
//
//            if (!cask.name.isEmpty()) {
//                out.setHoverName(Component.literal(cask.name));
//            }
//
//            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, out));
//        }
//
//        return true;
//    }

    public static ItemStack getFilledWaterBottle(int amount) {
        var out = new ItemStack(Items.POTION, amount);
        PotionUtils.setPotion(out, Potions.WATER);
        return out;
    }
}
