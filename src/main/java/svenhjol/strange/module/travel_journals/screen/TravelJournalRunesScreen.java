package svenhjol.strange.module.travel_journals.screen;

import svenhjol.strange.module.runestones.Runestones;
import svenhjol.strange.module.runestones.RunestonesClient;
import svenhjol.strange.module.runestones.RunestonesHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

@SuppressWarnings("ConstantConditions")
public class TravelJournalRunesScreen extends TravelJournalBaseScreen {
    private static final ResourceLocation SGA_TEXTURE = new ResourceLocation("minecraft", "alt");
    private List<Integer> learnedRunes;
    private int maxRunes;

    private final Style SGA_STYLE = Style.EMPTY.withFont(SGA_TEXTURE);

    public TravelJournalRunesScreen() {
        super(I18n.get("item.strange.travel_journal.runes"));
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();

        learnedRunes = RunestonesHelper.getLearnedRunes(minecraft.player);
        maxRunes = RunestonesHelper.NUMBER_OF_RUNES;
        previousPage = Page.RUNES;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!getClient().isPresent())
            return;

        super.render(matrices, mouseX, mouseY, delta);

        Player player = this.minecraft.player;
        String title;

        if (learnedRunes.size() >= RunestonesHelper.NUMBER_OF_RUNES) {
            title = I18n.get("gui.strange.travel_journal.learned_all_runes");
        } else{
            title = I18n.get("gui.strange.travel_journal.learned_runes", learnedRunes.size(), maxRunes);
        }

        int mid = this.width / 2;
        int top = 34;
        int left = mid - 62;

        // draw title
        centeredString(matrices, font, title, mid, titleTop, TEXT_COLOR);

        int index = 0;

        for (int sx = 0; sx <= 4; sx++) {
            for (int sy = 0; sy < 7; sy++) {
                if (index < Runestones.RUNESTONE_BLOCKS.size()) {
                    boolean knownRune = RunestonesHelper.hasLearnedRune(player, index);
                    Component runeText;
                    Component hoverText = null;
                    int color;
                    ItemStack itemStack;

                    if (knownRune) {
                        itemStack = new ItemStack(Runestones.RUNESTONE_BLOCKS.get(index));

                        if (RunestonesClient.CACHED_DESTINATION_NAMES.containsKey(index))
                            hoverText = new TranslatableComponent("runestone.strange.known", RunestonesClient.CACHED_DESTINATION_NAMES.get(index));

                        String runeChar = Character.toString((char) (index + 97));
                        runeText = new TextComponent(runeChar).withStyle(SGA_STYLE);
                        color = 0x000000;
                    } else {
                        itemStack = new ItemStack(Blocks.WHITE_STAINED_GLASS);
                        runeText = new TextComponent("?");
                        color = 0xc0c0c0;
                    }

                    int ix = left + (sx * 36);
                    int iy = top + (sy * 18);

                    itemRenderer.renderGuiItem(itemStack, ix, iy);

                    if (knownRune && hoverText != null) {
                        if (mouseX > ix && mouseX < ix + 16 && mouseY > iy && mouseY < iy + 16)
                            renderTooltip(matrices, hoverText, mouseX, mouseY);
                    }

                    font.draw(matrices, runeText, left + 18 + (sx * 36), top + 4 + (sy * 18), color);
                }
                index++;
            }
        }
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addRenderableWidget(new Button((width / 2) - (w / 2), y, w, h, new TranslatableComponent("gui.strange.travel_journal.close"), button -> onClose()));
    }
}
