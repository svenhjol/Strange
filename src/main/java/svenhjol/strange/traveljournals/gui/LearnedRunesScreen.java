package svenhjol.strange.traveljournals.gui;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.runestones.RunestonesClient;
import svenhjol.strange.runestones.RunestonesHelper;
import svenhjol.strange.traveljournals.TravelJournals;
import svenhjol.strange.traveljournals.TravelJournalsClient;

import java.util.List;

public class LearnedRunesScreen extends BaseScreen {
    private static final Identifier SGA_TEXTURE = new Identifier("minecraft", "alt");

    private int titleTop = 15;

    private final Style SGA_STYLE = Style.EMPTY.withFont(SGA_TEXTURE);

    public LearnedRunesScreen() {
        super(I18n.translate("item.strange.travel_journal.runes"));
        this.passEvents = false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TravelJournalsClient.closeIfNotHolding(this.client);

        if (!isClientValid())
            return;

        super.render(matrices, mouseX, mouseY, delta);

        PlayerEntity player = this.client.player;
        List<Integer> learnedRunes = RunestonesHelper.getLearnedRunes(player);
        int maxRunes = RunestonesHelper.NUMBER_OF_RUNES;
        String title;

        if (learnedRunes.size() >= RunestonesHelper.NUMBER_OF_RUNES) {
            title = I18n.translate("gui.strange.travel_journal.learned_all_runes");
        } else{
            title = I18n.translate("gui.strange.travel_journal.learned_runes", learnedRunes.size(), maxRunes);
        }

        int mid = this.width / 2;

        // draw title
        centredString(matrices, textRenderer, title, mid, titleTop, TEXT_COLOR);

        int top = 34;
        int left = mid - 62;

        int index = 0;

        for (int sx = 0; sx <= 4; sx++) {
            for (int sy = 0; sy < 7; sy++) {
                if (index < Runestones.RUNESTONE_BLOCKS.size()) {
                    boolean knownRune = RunestonesHelper.hasLearnedRune(player, index);
                    Text runeText;
                    Text hoverText = null;
                    int color;
                    ItemStack itemStack;

                    if (knownRune) {
                        itemStack = new ItemStack(Runestones.RUNESTONE_BLOCKS.get(index));

                        if (RunestonesClient.DESTINATION_NAMES.containsKey(index))
                            hoverText = new TranslatableText("runestone.strange.known", RunestonesClient.DESTINATION_NAMES.get(index));

                        String runeChar = Character.toString((char) (index + 97));
                        runeText = new LiteralText(runeChar).fillStyle(SGA_STYLE);
                        color = 0x000000;
                    } else {
                        itemStack = new ItemStack(Blocks.WHITE_STAINED_GLASS);
                        runeText = new LiteralText("?");
                        color = 0xc0c0c0;
                    }

                    int ix = left + (sx * 36);
                    int iy = top + (sy * 18);


                    itemRenderer.renderGuiItemIcon(itemStack, ix, iy);

                    if (knownRune && hoverText != null) {
                        if (mouseX > ix && mouseX < ix + 16 && mouseY > iy && mouseY < iy + 16)
                            renderTooltip(matrices, hoverText, mouseX, mouseY);
                    }

                    textRenderer.draw(matrices, runeText, left + 18 + (sx * 36), top + 4 + (sy * 18), color);
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

        this.addButton(new ButtonWidget((width / 2) - (w / 2), y, w, h, new TranslatableText("gui.strange.travel_journal.back"), button -> this.backToMainScreen()));
    }

    private void backToMainScreen() {
        if (client != null) {
            client.openScreen(null);
            TravelJournalsClient.sendServerPacket(TravelJournals.MSG_SERVER_OPEN_JOURNAL, null);
        }
    }
}
