package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import svenhjol.strange.module.journals.data.JournalLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JournalChooseIconScreen extends BaseJournalScreen {
    protected JournalLocation location;
    protected ItemStack selected;
    protected List<ItemLike> icons = new ArrayList<>();

    protected int perRow;
    protected int maxRows;
    protected int xOffset;
    protected int yOffset;
    protected int left;
    protected int top;

    public JournalChooseIconScreen(JournalLocation location) {
        super(new TranslatableComponent("gui.strange.journal.choose_icon"));

        this.location = location;

        // populate the icons
        icons.addAll(Arrays.asList(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.COBBLESTONE,
            Blocks.STONE_BRICKS,
            Blocks.SAND,
            Blocks.END_STONE,
            Blocks.BLUE_ICE,
            Blocks.NETHERRACK,

            Blocks.OAK_LOG,
            Blocks.OAK_PLANKS,
            Blocks.OAK_LEAVES,
            Blocks.BAMBOO,
            Blocks.BIG_DRIPLEAF,
            Blocks.MAGMA_BLOCK,
            Blocks.TERRACOTTA,
            Blocks.GLASS,

            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.ANVIL,
            Blocks.SPAWNER,
            Blocks.IRON_ORE,
            Blocks.DIAMOND_ORE,
            Items.TORCH,
            Items.REDSTONE_TORCH,

            Items.RED_BED,
            Items.CHEST,
            Items.BELL,
            Items.IRON_INGOT,
            Items.IRON_PICKAXE,
            Items.LAVA_BUCKET,
            Items.WATER_BUCKET,
            Items.COMPASS,

            Items.RED_DYE,
            Items.ORANGE_DYE,
            Items.YELLOW_DYE,
            Items.GREEN_DYE,
            Items.BLUE_DYE,
            Items.PURPLE_DYE,
            Items.BLACK_DYE,
            Items.WHITE_DYE
        ));

        perRow = 8;
        maxRows = 5;
        xOffset = 20;
        yOffset = 20;
        left = width - ((perRow * xOffset) / 2) + 2;
        top = 62;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        int index = 0;
        int mid = width / 2;

        for (int y = 0; y < maxRows; y++) {
            for (int x = 0; x < perRow; x++) {
                if (index >= icons.size()) continue;
                ItemStack stack = new ItemStack(icons.get(index));

                if (ItemStack.isSame(location.getIcon(), stack))
                    fill(poseStack, mid + left + x, top + y, mid + left + x + 16, top + y + 16, 0x9F9F9640);

                itemRenderer.renderGuiItem(stack, mid + left + (x * xOffset), top + (y * yOffset));
                index++;
            }
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int index = 0;
        int mid = width / 2;

        for (int cy = 0; cy < maxRows; cy++) {
            for (int cx = 0; cx < perRow; cx++) {
                if (index >= icons.size()) continue;
                if (x >= (mid + left + cx) && x < (mid + left + cx + xOffset)
                    && y >= (top + cy) && y < (top + cy + yOffset)) {
                    selected = new ItemStack(icons.get(index));
                    break;
                }
            }
        }

        if (selected != null)
            saveAndGoBack();

        return super.mouseClicked(x, y, button);
    }

    protected void saveAndGoBack() {
        // TODO: save here
        if (selected != null)
            location.setIcon(selected);

        if (minecraft != null)
            minecraft.setScreen(new JournalLocationScreen(location));
    }
}
