package svenhjol.strange.feature.quests.treasure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.quests.treasure.TreasureQuest.TreasureItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreasureQuestRenderer extends BaseQuestRenderer<TreasureQuest> {
    int spriteSelection = 0;

    @Override
    public int getPagedOfferHeight() {
        return super.getPagedOfferHeight();
    }

    @Override
    public int getPagedActiveHeight() {
        return super.getPagedActiveHeight() + 10;
    }

    @Override
    public Component getRequirementText() {
        return QuestsResources.TREASURE_REQUIREMENT_TEXT;
    }

    @Override
    public Component getRewardText() {
        return QuestsResources.TREASURE_REWARD_TEXT;
    }

    @Override
    public ItemStack getQuestIcon() {
        return new ItemStack(Items.CHEST);
    }

    @Override
    protected void renderRequirements(GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;
        var level = Minecraft.getInstance().level;

        var xo = midX + xOffset + 1;
        var yo = yOffset;

        if (quest.lootTables.isEmpty()) {
            return;
        }

        if (level != null && level.getGameTime() % 20 == 0) {
            spriteSelection = level.getRandom().nextInt(quest.lootTables.size());
        }

        var lootSprite = quest.lootTables().get(spriteSelection);
        List<Component> lootTableNames = new ArrayList<>();
        lootTableNames.add(Component.translatable("gui.strange.quests.treasure_loot_tables"));

        lootTableNames.addAll(quest.lootTables.stream()
                .map(l -> Component.translatableWithFallback(
                        "loot_table." + l.getNamespace() + "." + l.getPath().replace("/", "."),
                        niceLootTableName(l)))
                .collect(Collectors.toCollection(ArrayList::new)));

        var sprite = QuestsResources.LOOT_SPRITES.get(lootSprite);

        switch (screenType) {
            case PAGED_ACTIVE, SELECTED_ACTIVE:
                guiGraphics.fill(xo, yo - 2, xo + 20, yo + 30, requirementBackgroundColor);

                if (sprite != null) {
                    guiGraphics.blitSprite(sprite, xo + 2, yo + 6, 16, 16);
                } else {
                    guiGraphics.renderFakeItem(new ItemStack(Items.CHEST), xo, yo + 6);
                }

                if (mouseX >= xo && mouseX <= xo + 16
                    && mouseY >= yo + 6 && mouseY <= yo + 28) {
                    guiGraphics.renderComponentTooltip(font, lootTableNames, xo, yo + 27);
                }

                xo += 22;
                break;

            case PAGED_OFFER:
                if (sprite != null) {
                    guiGraphics.blitSprite(sprite, xo, yo, 16, 16);
                } else {
                    guiGraphics.renderFakeItem(new ItemStack(Items.CHEST), xo, yo);
                }

                if (mouseX >= xo && mouseX <= xo + 16
                    && mouseY >= yo && mouseY <= yo + 16) {
                    guiGraphics.renderComponentTooltip(font, lootTableNames, xo, yo + 27);
                }

                xo += 20;
                break;
        }

        for (var requirement : quest.requirements()) {
            if (requirement instanceof TreasureItem i) {
                Component label;
                if (showRemaining) {
                    label = TextHelper.translatable("gui.strange.quests.remaining", Math.max(0, i.total() - i.remaining()), i.total());
                } else {
                    label = TextHelper.translatable("gui.strange.quests.amount", i.total());
                }

                int width;

                switch (screenType) {
                    case PAGED_ACTIVE, SELECTED_ACTIVE:
                        guiGraphics.drawString(font, label, xo + 4, yo + 20, i.satisfied() ? satisfiedColor : requirementColor, false);
                        width = font.width(label);

                        guiGraphics.fill(xo, yo - 2, xo + width + 6, yo + 30, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);
                        guiGraphics.renderFakeItem(i.item, xo + (width / 2) - 5, yo);

                        if (mouseX >= xo + (width / 2) - 5 && mouseX <= xo + (width / 2) + 10
                            && mouseY >= yo && mouseY <= yo + 16) {
                            guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                        }

                        xo += 8 + width;
                        break;

                    case PAGED_OFFER:
                        guiGraphics.drawString(font, label, xo + 4, yo + 4, i.satisfied() ? satisfiedColor : requirementColor, false);
                        width = font.width(label);

                        guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);
                        guiGraphics.renderFakeItem(i.item, xo + 7 + width, yo);

                        if (mouseX >= xo && mouseX <= xo + 25 + width
                            && mouseY >= yo - 1 && mouseY <= yo + 17) {
                            guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                        }

                        xo += 27 + width;
                        break;
                }

            }
        }

        renderSatisfied(guiGraphics, xo, yo + 5, mouseX, mouseY);
    }

    protected String niceLootTableName(ResourceLocation id) {
        var path = id.getPath();
        return TextHelper.featureName(path.substring(path.lastIndexOf("/") + 1));
    }
}
