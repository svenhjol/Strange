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
import svenhjol.meson.enums.ColorVariant;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.module.TravelJournal;

public class RuneEntryScreen extends BaseTravelJournalScreen {
    protected String name;
    protected Entry entry;
    protected String runicName;
    protected FontRenderer glyphs;
    protected ColorVariant cycleRuneColor;
    protected boolean atLeastOneRune;

    public RuneEntryScreen(Entry entry, PlayerEntity player, Hand hand) {
        super(entry.name, player, hand);
        this.entry = entry;
        this.name = entry.name;
        this.passEvents = false;
        this.cycleRuneColor = ColorVariant.WHITE;
    }

    @Override
    protected void init() {
        super.init();
        if (mc == null || mc.world == null) return;
        if (!mc.world.isRemote) return;
        this.glyphs = mc.getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
        this.runicName = RunestoneHelper.getDiscoveredRunesClient(entry);
        this.atLeastOneRune = !this.runicName.equals("????????????");
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

            if (runicName.length() == 12) {
                int index = 0;
                int hpos = -2;
                int vpos = 4;
                int letterSpacing = 18;
                int midoffset = mid - 4;
                int background = 0xD2100026;

                for (int j = 0; j < runicName.length(); j++) {
                    letter = runicName.substring(j, j+1);
                    int fontvpush = 5;
                    boolean up = index <= 3;
                    boolean down = index >= 6 && index <= 9;
                    boolean right = index >= 3 && index <= 6;
                    boolean left = index >= 9 && index <= 12;
                    boolean unknown = letter.equals("?");
                    FontRenderer f = (unknown ? this.font : this.glyphs);

                    if (up) vpos--;
                    if (down) vpos++;
                    if (right) hpos++;
                    if (left) hpos--;

                    final int ox = hpos * letterSpacing;
                    final int oy = offset + (vpos * letterSpacing);

                    AbstractGui.fill(midoffset + ox, offset + oy, midoffset + ox + letterSpacing-1, offset + oy + letterSpacing-1, background);

                    if (letter.equals("f"))
                        fontvpush = 7;

                    if (index == 10 && stack != ItemStack.EMPTY)
                        this.blitItemIcon(stack, midoffset + ox, offset + oy + letterSpacing);

                    this.drawCenteredString(f, letter, midoffset + ox + 9, offset + oy + fontvpush, unknown ? 0xFF727272 : 0xFFFFFFFF);
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
