package svenhjol.strange.feature.quests.artifact;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.quests.artifact.ArtifactQuest.ArtifactItem;

public class ArtifactQuestRenderer extends BaseQuestRenderer<ArtifactQuest> {
    @Override
    public int getPagedOfferHeight() {
        return super.getPagedOfferHeight();
    }

    @Override
    public int getPagedActiveHeight() {
        return super.getPagedActiveHeight();
    }

    @Override
    public Component getRequirementText() {
        return QuestsResources.ARTIFACT_REQUIREMENT_TEXT;
    }

    @Override
    public Component getRewardText() {
        return QuestsResources.ARTIFACT_REWARD_TEXT;
    }

    @Override
    public ItemStack getQuestIcon() {
        return new ItemStack(Items.SPYGLASS);
    }

    @Override
    protected void renderRequirements(GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;

        var xo = midX + xOffset + 1;
        var yo = yOffset;

        for (int i = 0; i < Math.min(3, quest.lootTables().size()); i++) {
            var lootTable = quest.lootTables().get(i);

            var sprite = QuestsResources.LOOT_SPRITES.get(lootTable);
            if (sprite != null) {
                guiGraphics.blitSprite(sprite, xo, yo, 16, 16);
            } else {
                guiGraphics.renderFakeItem(new ItemStack(Items.CHEST), xo, yo);
            }

            if (mouseX >= xo && mouseX <= xo + 16
                && mouseY >= yo && mouseY <= yo + 16) {
                var component = Component.translatableWithFallback("loot_table." + lootTable.getNamespace() + "." + lootTable.getPath().replace("/", "."),
                    niceLootTableName(lootTable));
                guiGraphics.renderTooltip(font, component, xo, yo + 27);
            }

            xo += 17;
        }

        xo += 2;

        for (var requirement : quest.requirements()) {
            if (requirement instanceof ArtifactItem i) {
                Component label;
                if (showRemaining) {
                    label = TextHelper.translatable("gui.strange.quests.remaining", Math.max(0, i.total() - i.remaining()), i.total());
                } else {
                    label = TextHelper.translatable("gui.strange.quests.amount", i.total());
                }

                guiGraphics.drawString(font, label, xo + 4, yo + 4, i.satisfied() ? satisfiedColor : requirementColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);
                guiGraphics.renderFakeItem(i.item, xo + 7 + width, yo);

                if (mouseX >= xo && mouseX <= xo + 25 + width
                    && mouseY >= yo - 1 && mouseY <= yo + 17) {
                    guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                }

                xo += 27 + width;
            }
        }

        renderSatisfied(guiGraphics, xo, yo, mouseX, mouseY);
    }

    protected String niceLootTableName(ResourceLocation id) {
        var path = id.getPath();
        return TextHelper.featureName(path.substring(path.lastIndexOf("/") + 1));
    }
}
