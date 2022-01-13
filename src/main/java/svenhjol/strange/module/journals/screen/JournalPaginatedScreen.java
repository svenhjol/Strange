package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import svenhjol.strange.module.journals.paginator.BasePaginator;

import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public abstract class JournalPaginatedScreen<T> extends JournalScreen {
    protected BasePaginator<T> paginator;
    protected int y = 40;

    public JournalPaginatedScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        // Instantiate and initialize the paginator. The paginator takes two consumers:
        //  1. Consume a single click on a listed item.
        //  2. Consume a new page offset when the left or right page buttons are clicked.
        paginator = getPaginator();
        paginator.init(this, offset, midX, y, onClick(), onNewOffset());
        paginator.setButtonWidth(180);

        addButtons();
        setViewedPage();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        paginator.render(poseStack, itemRenderer, font);
    }

    /**
     * Consumer for a click on a listed item.
     */
    protected abstract Consumer<T> onClick();

    /**
     * Consumer for a new page offset after clicking on a page arrow.
     */
    protected Consumer<Integer> onNewOffset() {
        return newOffset -> {
            offset = newOffset;
            init(minecraft, width, height);
        };
    }

    protected void addButtons() {
        // no op
    }

    protected abstract BasePaginator<T> getPaginator();

    protected abstract void setViewedPage();
}
