package svenhjol.strange.traveljournal.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmClient;
import svenhjol.meson.Meson;
import svenhjol.meson.enums.ColorVariant;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.module.TravelJournal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuneEntryScreen extends BaseTravelJournalScreen {
    protected String name;
    protected Entry entry;
    protected char[] runicName;
    protected FontRenderer glyphs;
    protected ColorVariant cycleRuneColor;
    protected boolean atLeastOneRune;

    public RuneEntryScreen(Entry entry, PlayerEntity player, Hand hand) {
        super(entry.name, player, hand);
        this.entry = entry;
        this.name = entry.name;
        this.passEvents = false;
        this.runicName = new char[]{};
        this.cycleRuneColor = ColorVariant.WHITE;
    }

    @Override
    protected void init() {
        super.init();
        if (mc == null) return;
        if (!mc.world.isRemote) return;

        if (player.isCreative() || !Strange.client.discoveredRunes.isEmpty()) {
            this.glyphs = mc.getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
            int dim = entry.dim;
            String posref = entry.posref;
            Map<Character, Character> runeCharMap = RunestoneHelper.getRuneCharMap();
            List<Character> values = new ArrayList<>(runeCharMap.values());

            StringBuilder assembled = new StringBuilder();
            char[] chars = posref.toCharArray();

            for (int i = 0; i < chars.length; i++) {
                boolean showRune = false;
                char c = chars[i];
                Character letter = runeCharMap.get(c);

                if (values.contains(letter)) {
                    int runeValue = values.indexOf(letter);
                    if (player.isCreative() || Strange.client.discoveredRunes.contains(runeValue)) {
                        showRune = true;
                    }
                }

                if (!showRune) {
                    letter = '?';
                } else {
                    this.atLeastOneRune = true;
                }

                assembled.append(letter);
                Meson.LOG.debug(String.valueOf(letter));
            }

            if (this.atLeastOneRune) {
                this.runicName = assembled.toString().toCharArray();
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        TravelJournal.client.closeIfNotHolding(mc, player, hand);

        int mid = this.width / 2;
        int y = 20;
        renderBackgroundTexture();

        if (this.atLeastOneRune && entry.pos != null) {
            int offset = y + 1;
            String letter;
            ItemStack stack;

            if (CharmClient.clientTicks % 30 == 0) {
                ColorVariant currentRuneColor = this.cycleRuneColor;
                int c = currentRuneColor.ordinal();
                cycleRuneColor = ColorVariant.byIndex(c == 15 ? 0 : ++c);
            }
            stack = Charm.quarkCompat != null ? Charm.quarkCompat.getRune(cycleRuneColor) : new ItemStack(Items.DIAMOND);

            if (runicName.length > 0) {
                int index = 0;
                int hpos = -2;
                int vpos = 4;
                int letterSpacing = 18;
                int midoffset = mid - 4;
                int background = 0xD2050014;

                for (int j = 0; j < runicName.length; j++) {
                    letter = String.valueOf(runicName[j]);
                    int vpush = 5;
                    boolean up = index <= 3;
                    boolean down = index >= 6 && index <= 9;
                    boolean right = index >= 3 && index <= 6;
                    boolean left = index >= 9 && index <= 12;
                    boolean q = letter.equals("?");
                    FontRenderer f = (q ? this.font : this.glyphs);

                    if (up) vpos--;
                    if (down) vpos++;
                    if (right) hpos++;
                    if (left) hpos--;

                    final int ox = hpos * letterSpacing;
                    final int oy = offset + (vpos * letterSpacing);

                    AbstractGui.fill(midoffset + ox, offset + oy, midoffset + ox + letterSpacing-1, offset + oy + letterSpacing-1, background);

                    if (letter.equals("f"))
                        vpush = 7;

                    if (index == 10) {
                        if (stack != ItemStack.EMPTY)
                            this.blitItemIcon(stack, midoffset + ox, offset + oy + letterSpacing);
                    }

                    this.drawCenteredString(f, letter, midoffset + ox + 9, offset + oy + vpush, q ? 0xFF727272 : 0xFFFFFFFF);
                    index++;
                }
            }
        } else {
            this.drawCenteredString(this.font, "????", mid + 4, 60, 0xFF404040);
        }

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addButton(new Button((width / 2) - (w / 2), y, w, h, I18n.format("gui.strange.travel_journal.back"), (button) -> this.back()));
    }

    private void back() {
        mc.displayGuiScreen(new UpdateEntryScreen(entry, player, hand));
    }
}
