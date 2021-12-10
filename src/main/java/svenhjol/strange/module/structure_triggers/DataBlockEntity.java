package svenhjol.strange.module.structure_triggers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;

import java.util.HashMap;
import java.util.Map;

public class DataBlockEntity extends CharmSyncedBlockEntity {
    public static final String METADATA_TAG = "metadata";
    public static final Map<String, ItemLike> DECORATION_ITEM_MAP = new HashMap<>();

    private String metadata = "";
    private ItemStack cachedItem = null;
    public float displayTicks = 0.0F;

    public DataBlockEntity(BlockPos pos, BlockState state) {
        super(StructureTriggers.DATA_BLOCK_ENTITY, pos, state);

        DECORATION_ITEM_MAP.put("anvil", Blocks.ANVIL);
        DECORATION_ITEM_MAP.put("armor", Items.ARMOR_STAND);
        DECORATION_ITEM_MAP.put("carpet", Blocks.RED_CARPET);
        DECORATION_ITEM_MAP.put("cauldron", Blocks.CAULDRON);
        DECORATION_ITEM_MAP.put("entity", Items.SHULKER_SPAWN_EGG);
        DECORATION_ITEM_MAP.put("flower", Blocks.DANDELION);
        DECORATION_ITEM_MAP.put("lantern", Blocks.LANTERN);
        DECORATION_ITEM_MAP.put("lava", Blocks.MAGMA_BLOCK);
        DECORATION_ITEM_MAP.put("mob", Blocks.SKELETON_SKULL);
        DECORATION_ITEM_MAP.put("ore", Blocks.IRON_ORE);
        DECORATION_ITEM_MAP.put("flowerpot", Blocks.POTTED_DANDELION);
        DECORATION_ITEM_MAP.put("sapling", Blocks.OAK_SAPLING);
        DECORATION_ITEM_MAP.put("spawner", Blocks.SPAWNER);
        DECORATION_ITEM_MAP.put("storage", Blocks.BARREL);
        DECORATION_ITEM_MAP.put("chest", Blocks.CHEST);
        DECORATION_ITEM_MAP.put("barrel", Blocks.BARREL);
        DECORATION_ITEM_MAP.put("block", Blocks.COBBLESTONE);
        DECORATION_ITEM_MAP.put("decoration", Blocks.SMITHING_TABLE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(METADATA_TAG, metadata);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        metadata = tag.getString(METADATA_TAG);
    }

    public ItemStack getItem() {
        if (cachedItem == null) {
            for (String s : DECORATION_ITEM_MAP.keySet()) {
                if (metadata.startsWith(s)) {
                    cachedItem = new ItemStack(DECORATION_ITEM_MAP.get(s));
                }
            }

            if (cachedItem == null) {
                cachedItem = new ItemStack(Blocks.STRUCTURE_BLOCK);
            }
        }

        return cachedItem;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public void setChanged() {
        cachedItem = null;
        super.setChanged();
    }
}
