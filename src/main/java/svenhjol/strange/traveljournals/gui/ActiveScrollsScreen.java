package svenhjol.strange.traveljournals.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import svenhjol.strange.base.helper.NetworkHelper;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.scrolls.ScrollsClient;
import svenhjol.strange.scrolls.tag.Quest;
import svenhjol.strange.traveljournals.TravelJournals;
import svenhjol.strange.traveljournals.TravelJournalsClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class ActiveScrollsScreen extends BaseScreen {
    private List<Quest> currentQuests;
    private int titleTop = 15;

    private List<Integer> hasRenderedTrashButtons = new ArrayList<>();
    private Map<Pair<Integer, Integer>, Runnable> scrollButtons = new HashMap<>();

    public ActiveScrollsScreen() {
        super(I18n.translate("item.strange.travel_journal.active_scrolls"));
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();

        this.hasRenderedTrashButtons.clear();
        this.scrollButtons.clear();
        this.currentQuests = ScrollsClient.CACHED_CURRENT_QUESTS;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TravelJournalsClient.closeIfNotHolding(this.client);

        if (!isClientValid())
            return;

        super.render(matrices, mouseX, mouseY, delta);

        PlayerEntity player = this.client.player;
        String title;

        if (currentQuests.size() == 0) {
            title = I18n.translate("gui.strange.travel_journal.no_active_scrolls");
        } else {
            title = I18n.translate("gui.strange.travel_journal.active_scrolls");
        }

        int mid = this.width / 2;
        int top = 34;
        int left = mid - 86;

        // draw title
        centeredString(matrices, textRenderer, title, mid, titleTop, TEXT_COLOR);

        for (int i = 0; i < currentQuests.size(); i++) {
            Quest quest = currentQuests.get(i);

            int qy = top + (i * 20);

            String questTitle = quest.getTitle();
            int tier = quest.getTier();

            if (!Scrolls.SCROLL_TIERS.containsKey(tier))
                continue;

            ItemStack stack = new ItemStack(Scrolls.SCROLL_TIERS.get(tier));
            itemRenderer.renderGuiItemIcon(stack, left, qy);
            textRenderer.draw(matrices, questTitle, left + 20, qy + 4, 0);

            // button to open scroll
            Pair<Integer, Integer> buttonTopLeft = new Pair<>(left, qy);
            if (!scrollButtons.containsKey(buttonTopLeft)) {
                scrollButtons.put(buttonTopLeft, () -> {
                    NetworkHelper.sendPacketToServer(Scrolls.MSG_SERVER_OPEN_SCROLL, buffer
                        -> buffer.writeString(quest.getId()));
                });
            }

            // button to abandon quest
            if (!hasRenderedTrashButtons.contains(i)) {
                this.addButton(new TexturedButtonWidget(mid + 75, qy - 1, 20, 18, 160, 0, 19, BUTTONS, r -> abandonQuest(quest)));
                hasRenderedTrashButtons.add(i);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Pair<Integer, Integer> pair : scrollButtons.keySet()) {
            if (mouseX > pair.getLeft() && mouseX < pair.getLeft() + 16
                && mouseY > pair.getRight() && mouseY < pair.getRight() + 16) {
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

        this.addButton(new ButtonWidget((width / 2) - (w / 2), y, w, h, new TranslatableText("gui.strange.travel_journal.back"), button -> this.backToMainScreen()));
    }

    private void backToMainScreen() {
        if (client != null) {
            client.openScreen(null);
            NetworkHelper.sendEmptyPacketToServer(TravelJournals.MSG_SERVER_OPEN_JOURNAL);
        }
    }

    private void abandonQuest(Quest quest) {
        NetworkHelper.sendPacketToServer(Scrolls.MSG_SERVER_ABANDON_QUEST, buffer -> buffer.writeString(quest.getId()));
        init();
    }
}
