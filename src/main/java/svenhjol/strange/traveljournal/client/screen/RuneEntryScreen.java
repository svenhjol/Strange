package svenhjol.strange.traveljournal.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.StringUtils;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmClient;
import svenhjol.meson.Meson;
import svenhjol.meson.enums.ColorVariant;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.module.TravelJournal;

public class RuneEntryScreen extends BaseTravelJournalScreen {
    protected String name;
    protected Entry entry;
    protected char[] runicName;
    protected FontRenderer glyphs;
    protected ColorVariant cycleRuneColor;

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
            int dim = 128 + entry.dim;
            if (dim >= 0 && dim < 256) {
                String dimHex = Integer.toHexString(dim);

                if (dimHex.length() % 2 == 1)
                    dimHex = "0" + dimHex;

                // get first and second chars of dimHex
                String d0 = dimHex.substring(0, 1);
                String d1 = dimHex.substring(1, 2);

                // convert the entry pos to hex
                String posHex = Long.toHexString(entry.pos.toLong());
                posHex = StringUtils.leftPad(posHex, 4, "0");

                // interpolate dimHex within posHex
                String hex = d0 + posHex + d1;
                StringBuilder assembled = new StringBuilder();

                char[] chars = hex.toCharArray();
                int rune = -1;
                boolean atLeastOneRune = false;

                for (int i = 0; i < chars.length; i++) {
                    String letter;
                    char c = chars[i];
                    int v = Character.getNumericValue(c);

                    if (i % 2 == 0) {
                        // first time round set the rune value
                        rune = v;
                    } else {
                        // second time round set the color and add the string
                        if (rune >= 0) {
                            final String color = Integer.toHexString(v);

                            if (player.isCreative() || Strange.client.discoveredRunes.contains(rune)) {
                                letter = RunestoneHelper.getRuneIntCharMap().get(rune).toString();
                                atLeastOneRune = true;
                            } else {
                                letter = "?";
                            }

                            assembled.append(letter);
                            assembled.append(color);
                            Meson.LOG.debug("(" + letter + color + ")");
                        }

                        rune = -1;
                    }
                }

                if (atLeastOneRune) {
                    this.runicName = assembled.toString().toCharArray();
                }
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        TravelJournal.client.closeIfNotHolding(mc, player, hand);

        int mid = this.width / 2;
        int y = 20;
        renderBackgroundTexture();


        if (entry.pos != null) {
            int offset = y + 1;
            String letter = null;

            if (runicName.length > 0) {
                // draw background for rune layout

                int index = 0;
                int hpos = -2;
                int vpos = 4;
                int letterSpacing = 20;
                int midoffset = mid - 4;
                int background = 0xBB020010;

                for (int j = 0; j < runicName.length; j++) {
                    if (j % 2 == 0) {
                        letter = String.valueOf(runicName[j]);
                    } else {
                        String colHex = String.valueOf(runicName[j]);
                        int colNum = Integer.parseUnsignedInt(colHex, 16);
                        boolean q = letter.equals("?");
                        FontRenderer f = (q ? this.font : this.glyphs);

                        boolean up = index <= 3;
                        boolean down = index >= 6;
                        boolean hadd = index >= 3 && index <= 6;
                        boolean toprow = index >= 3 && index <= 5;

                        if (up)
                            vpos--;

                        if (down)
                            vpos++;

                        if (hadd)
                            hpos++;

                        final int ox = hpos * letterSpacing;
                        final int oy = offset + (vpos * letterSpacing);

                        AbstractGui.fill(midoffset + ox, offset + oy, midoffset + ox + letterSpacing-1, offset + oy + letterSpacing-1, background);
                        this.drawCenteredString(f, letter, midoffset + ox + 10, offset + oy + 6, q ? 0x888888 : 0xFFFFFF);

                        if (!q) {
                            int dx = midoffset + (ox + (ox / 2));
                            int dy = offset + oy;
                            if (toprow) {
                                dx = midoffset + ox;
                                dy = offset + oy - letterSpacing + 2;
                            }
                            ItemStack dyeItem = new ItemStack(DyeItem.getItem(DyeColor.byId(colNum)));
                            this.blitItemIcon(dyeItem, dx, dy);
                        }

                        index++;
                    }
                }

                hpos = -1;
                vpos = 4;

                for (int i = 0; i <= 2; i++) {
                    ItemStack stack = new ItemStack(DyeItem.getItem(DyeColor.byId(0)));
                    int useBackground = background;

                    if (i == 0) {
                        letter = "a";
                    } else if (i == 1) {
                        useBackground = 0xAA10702A;
                        letter = "";
                        if (CharmClient.clientTicks % 30 == 0) {
                            ColorVariant currentRuneColor = this.cycleRuneColor;
                            int c = currentRuneColor.ordinal();
                            cycleRuneColor = ColorVariant.byIndex(c == 15 ? 0 : ++c);
                        }

                        stack = Charm.quarkCompat != null ? Charm.quarkCompat.getRune(cycleRuneColor) : new ItemStack(Items.DIAMOND);
                    } else {
                        letter = "z";
                    }

                    final int ox = hpos * letterSpacing;
                    final int oy = offset + (vpos * letterSpacing);
                    AbstractGui.fill(midoffset + ox, offset + oy, midoffset + ox + letterSpacing - 1, offset + oy + letterSpacing - 1, useBackground);
                    if (!letter.isEmpty())
                        this.drawCenteredString(this.glyphs, letter, midoffset + ox + 10, offset + oy + 6, 0xFFFFFF);

                    this.blitItemIcon(stack, midoffset + ox, offset + oy + letterSpacing);

                    hpos += 1;
                }
            }
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
        mc.displayGuiScreen(new TravelJournalScreen(player, hand));
    }
}
