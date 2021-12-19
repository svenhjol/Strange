package svenhjol.strange.module.journals2.paginator;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.screen.JournalScreen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class Paginator<T> {
    private final List<T> items;
    private int pageOffset;
    private int x;
    private int y;

    protected int perPage;
    protected int currentPage;
    protected int yOffset;
    protected int yControls;
    protected int buttonWidth;
    protected int buttonHeight;

    private List<T> sublist;

    public Paginator(List<T> items) {
        this.perPage = 6;
        this.buttonHeight = 20;
        this.buttonWidth = 140;
        this.yOffset = 21;
        this.yControls = 180;
        this.items = items;
    }

    public void init(Screen screen, int pageOffset, int x, int y, Consumer<Integer> onRedraw) {
        this.pageOffset = pageOffset;
        this.currentPage = pageOffset - 1;

        this.x = x;
        this.y = y;

        int size = items.size();

        if (size > perPage) {
            if (currentPage * perPage >= size || currentPage * perPage < 0) {
                this.pageOffset = 1;
                currentPage = 0;
            }
            sublist = items.subList(currentPage * perPage, Math.min(currentPage * perPage + perPage, size));
        } else {
            sublist = items;
        }

        // On the first pass we add all the buttons to the screen.
        for (int i = 0; i < sublist.size(); i++) {
            T item = sublist.get(i);
            var name = getItemName(item);
            var action = getItemClickAction(item);
            var button = new Button(x - (buttonWidth / 2) + 6, y + (i * yOffset), buttonWidth, buttonHeight, name, b -> action.accept(item));

            screen.addRenderableWidget(button);
        }

        if (size > perPage) {
            if (pageOffset * perPage < size) {
                var button = new ImageButton(x + 30, yControls, 20, 18, 120, 0, 18, JournalScreen.NAVIGATION, b -> {
                    onRedraw.accept(this.pageOffset + 1);
                });
                screen.addRenderableWidget(button);
            }
            if (pageOffset > 1) {
                var button = new ImageButton(x - 50, yControls, 20, 18, 140, 0, 18, JournalScreen.NAVIGATION, b -> {
                    onRedraw.accept(this.pageOffset - 1);
                });
                screen.addRenderableWidget(button);
            }
        }
    }

    public void render(PoseStack poseStack, ItemRenderer itemRenderer, Font font) {
        int size = items.size();

        for (int i = 0; i < sublist.size(); i++) {
            var icon = getItemIcon(sublist.get(i));

            if (icon != null) {
                itemRenderer.renderGuiItem(icon, x - (buttonWidth / 2) - 12, y + (i * yOffset) + 2);
            }
        }

        if (size > perPage) {
            GuiHelper.drawCenteredString(poseStack, font, getPageLabel(), x, yControls + 6, 0x908080);
        }

        if (size == 0) {
            GuiHelper.drawCenteredString(poseStack, font, getNoItemsLabel(), x, yControls - 80, 0x908080);
        }
    }

    protected Component getPageLabel() {
        return new TranslatableComponent("gui.strange.journal.page", pageOffset);
    }

    protected Component getNoItemsLabel() {
        return new TranslatableComponent("gui.strange.journal.nothing");
    }

    protected abstract Component getItemName(T item);

    protected abstract Consumer<T> getItemClickAction(T item);

    @Nullable
    protected abstract ItemStack getItemIcon(T item);
}
