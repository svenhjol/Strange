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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals2.screen.JournalScreen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public abstract class BasePaginator<T> {
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
    protected int distBetweenIconAndButton;
    protected int distBetweenPageButtons;


    protected BiConsumer<T, Button> onItemButtonRendered = (item, button) -> {};
    protected Function<T, Component> onItemHovered = null;

    private List<T> sublist;

    public BasePaginator(List<T> items) {
        this.perPage = 6;
        this.buttonHeight = 20;
        this.buttonWidth = 140;
        this.yOffset = 21;
        this.yControls = 180;
        this.distBetweenIconAndButton = 6;
        this.distBetweenPageButtons = 30;
        this.items = items;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public void setButtonHeight(int buttonHeight) {
        this.buttonHeight = buttonHeight;
    }

    public void setButtonWidth(int buttonWidth) {
        this.buttonWidth = buttonWidth;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public void setYControls(int yControls) {
        this.yControls = yControls;
    }

    public void setDistBetweenIconAndButton(int distBetweenIconAndButton) {
        this.distBetweenIconAndButton = distBetweenIconAndButton;
    }

    public void setDistBetweenPageButtons(int distBetweenPageButtons) {
        this.distBetweenPageButtons = distBetweenPageButtons;
    }

    /**
     * Define a function to be executed when creating a tooltip for the list item button.
     */
    public void setOnItemHovered(Function<T, Component> onItemHovered) {
        this.onItemHovered = onItemHovered;
    }

    /**
     * Define a consumer to be executed immediately after a list item has been rendered.
     */
    public void setOnItemButtonRendered(BiConsumer<T, Button> onItemButtonRendered) {
        this.onItemButtonRendered = onItemButtonRendered;
    }

    public void init(Screen screen, int pageOffset, int x, int y, Consumer<T> onClick, Consumer<Integer> onRedraw) {
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
            var hasIcon = getItemIcon(item) != null;

            // Set up a button tooltip if the onItemTooltip function is defined.
            Button.OnTooltip tooltip;
            if (onItemHovered != null) {
                tooltip = (button, poseStack, tx, ty) -> screen.renderTooltip(poseStack, onItemHovered.apply(item), tx, ty);
            } else {
                tooltip = Button.NO_TOOLTIP;
            }

            // Handle truncation if the text overflows the buttonWidth
            if ((name.getContents().length() * 6) > buttonWidth) {
                var max = buttonWidth / 6;
                var newName = name.getContents().substring(0, max);
                name = new TextComponent(newName);
            }

            // Add the button to the screen and run the onItemButtonRendered callback, allowing for manipulation of button state.
            var bx = x - (buttonWidth / 2) + (hasIcon ? distBetweenIconAndButton : 0);
            var by = y + (i * yOffset);
            var button = new Button(bx, by, buttonWidth, buttonHeight, name, b -> onClick.accept(item), tooltip);

            screen.addRenderableWidget(button);
            onItemButtonRendered.accept(item, button);
        }

        // Draw the pagination buttons at the bottom of the page (yControls is the y-offset for these).
        if (size > perPage) {
            if (pageOffset * perPage < size) {
                var button = new ImageButton(x + distBetweenPageButtons, yControls, 20, 18, 120, 0, 18, JournalScreen.NAVIGATION, b -> {
                    onRedraw.accept(this.pageOffset + 1);
                });
                screen.addRenderableWidget(button);
            }
            if (pageOffset > 1) {
                var button = new ImageButton(x - distBetweenPageButtons - 20, yControls, 20, 18, 140, 0, 18, JournalScreen.NAVIGATION, b -> {
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

    @Nullable
    protected abstract ItemStack getItemIcon(T item);
}
