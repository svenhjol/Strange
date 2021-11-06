package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.data.JournalLocation;

public class JournalChooseIconScreen extends JournalScreen {
    protected JournalLocation location;
    protected ItemStack selected;

    protected int perRow;
    protected int maxRows;
    protected int xOffset;
    protected int yOffset;
    protected int left;
    protected int top;

    public JournalChooseIconScreen(JournalLocation location) {
        super(CHOOSE_ICON);

        this.location = location;
        this.perRow = 8;
        this.maxRows = 5;
        this.xOffset = 20;
        this.yOffset = 20;
        this.left = width - ((perRow * xOffset) / 2) + 2;
        this.top = 62;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        int index = 0;

        for (int y = 0; y < maxRows; y++) {
            for (int x = 0; x < perRow; x++) {
                if (index >= Journals.locationIcons.size()) continue;
                ItemStack stack = new ItemStack(Journals.locationIcons.get(index));

                if (ItemStack.isSame(location.getIcon(), stack))
                    fill(poseStack, midX + left + (x * xOffset), top + (y * yOffset), midX + left + (x * xOffset) + 16, top + (y * yOffset) + 16, 0x9F9F9640);

                itemRenderer.renderGuiItem(stack, midX + left + (x * xOffset), top + (y * yOffset));
                index++;
            }
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int index = 0;

        for (int cy = 0; cy < maxRows; cy++) {
            for (int cx = 0; cx < perRow; cx++) {
                if (index >= Journals.locationIcons.size()) continue;
                if (x >= (midX + left + (cx * xOffset)) && x < (midX + left + (cx * xOffset) + 16)
                    && y >= (top + (cy * yOffset)) && y < (top + (cy * yOffset) + 16)) {
                    selected = new ItemStack(Journals.locationIcons.get(index));
                    break;
                }
                index++;
            }
        }

        if (selected != null) {
            saveAndGoBack();
            return true;
        }

        return super.mouseClicked(x, y, button);
    }

    protected void saveAndGoBack() {
        // TODO: save here
        if (selected != null) {
            location.setIcon(selected);
        }

        if (minecraft != null) {
            minecraft.setScreen(new JournalLocationScreen(location));
        }
    }
}
