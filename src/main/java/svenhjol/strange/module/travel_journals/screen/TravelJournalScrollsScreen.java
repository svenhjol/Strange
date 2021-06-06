package svenhjol.strange.module.travel_journals.screen;

import svenhjol.strange.helper.NetworkHelper;
import svenhjol.strange.module.scrolls.Scrolls;
import svenhjol.strange.module.scrolls.ScrollsClient;
import svenhjol.strange.module.scrolls.tag.Quest;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("ConstantConditions")
public class TravelJournalScrollsScreen extends TravelJournalBaseScreen {
    private List<Quest> currentQuests;
    private final List<Integer> hasRenderedTrashButtons = new ArrayList<>();
    private final Map<Tuple<Integer, Integer>, Runnable> scrollButtons = new HashMap<>();

    public TravelJournalScrollsScreen() {
        super(I18n.get("item.strange.travel_journal.active_scrolls"));
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();

        hasRenderedTrashButtons.clear();
        scrollButtons.clear();
        currentQuests = ScrollsClient.CACHED_CURRENT_QUESTS;
        previousPage = Page.SCROLLS;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!getClient().isPresent())
            return;

        super.render(matrices, mouseX, mouseY, delta);
        String title;

        if (currentQuests.size() == 0) {
            title = I18n.get("gui.strange.travel_journal.no_active_scrolls");
        } else {
            title = I18n.get("gui.strange.travel_journal.active_scrolls");
        }

        int mid = this.width / 2;
        int top = 34;
        int left = mid - 86;

        // draw title
        centeredString(matrices, font, title, mid, titleTop, TEXT_COLOR);

        for (int i = 0; i < currentQuests.size(); i++) {
            Quest quest = currentQuests.get(i);

            int qy = top + (i * 20);

            String questTitle = quest.getTitle();
            int tier = quest.getTier();

            if (!Scrolls.SCROLL_TIERS.containsKey(tier))
                continue;

            ItemStack stack = new ItemStack(Scrolls.SCROLL_TIERS.get(tier));
            itemRenderer.renderGuiItem(stack, left, qy);
            font.draw(matrices, questTitle, left + 20, qy + 4, 0);

            // button to open scroll
            Tuple<Integer, Integer> buttonTopLeft = new Tuple<>(left, qy);
            if (!scrollButtons.containsKey(buttonTopLeft)) {
                scrollButtons.put(buttonTopLeft, () -> {
                    NetworkHelper.sendPacketToServer(Scrolls.MSG_SERVER_OPEN_SCROLL, buffer -> {
                        buffer.writeUtf(quest.getId());
                    });
                });
            }

            // button to abandon quest
            if (!hasRenderedTrashButtons.contains(i)) {
                this.addRenderableWidget(new ImageButton(mid + 75, qy - 1, 20, 18, 160, 0, 19, BUTTONS, r -> abandonQuest(quest)));
                hasRenderedTrashButtons.add(i);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Tuple<Integer, Integer> pair : scrollButtons.keySet()) {
            if (mouseX > pair.getA() && mouseX < pair.getA() + 16
                && mouseY > pair.getB() && mouseY < pair.getB() + 16) {
                scrollButtons.get(pair).run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addRenderableWidget(new Button((width / 2) - (w / 2), y, w, h, new TranslatableComponent("gui.strange.travel_journal.close"), button -> onClose()));
    }

    private void abandonQuest(Quest quest) {
        NetworkHelper.sendPacketToServer(Scrolls.MSG_SERVER_ABANDON_QUEST, buffer -> buffer.writeUtf(quest.getId()));
        init();
    }
}
