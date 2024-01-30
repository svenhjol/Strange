package svenhjol.strange.feature.quests.client.renderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import svenhjol.charmony.feature.colored_glints.ColoredGlints;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.quest.BattleQuest;
import svenhjol.strange.feature.quests.quest.BattleQuest.BattleTarget;

public class BattleQuestRenderer extends BaseQuestRenderer<BattleQuest> {
    @Override
    public Component getRequirementText() {
        return QuestsResources.BATTLE_REQUIREMENT_TEXT;
    }

    @Override
    public Component getRewardText() {
        return QuestsResources.BATTLE_REWARD_TEXT;
    }

    @Override
    public ItemStack getQuestIcon() {
        var stack = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentHelper.enchantItem(RandomSource.create(), stack, 1, false);
        ColoredGlints.applyColoredGlint(stack, DyeColor.BLUE);
        return stack;
    }

    @Override
    protected void renderRequirements(GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;

        var xo = midX + xOffset;
        var yo = yOffset;

        for (var requirement : quest.requirements()) {
            if (requirement instanceof BattleTarget i) {
                Component label;
                if (showRemaining) {
                    label = TextHelper.translatable("gui.strange.quests.remaining", Math.max(0, i.total() - i.remaining()), i.total());
                } else {
                    label = TextHelper.translatable("gui.strange.quests.amount", i.total());
                }
                var entityId = BuiltInRegistries.ENTITY_TYPE.getKey(i.entity);

                guiGraphics.drawString(font, label, xo + 4, yo + 4, i.satisfied() ? satisfiedColor : requirementColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);

                var sprite = QuestsResources.MOB_SPRITES.get(i.entity);
                if (sprite != null) {
                    guiGraphics.blitSprite(sprite, xo + 7 + width, yo, 16, 16);
                } else {
                    ItemLike spawnEgg = SpawnEggItem.byId(i.entity);
                    if (spawnEgg == null) {
                        spawnEgg = Items.BLAZE_SPAWN_EGG;
                    }
                    guiGraphics.renderFakeItem(new ItemStack(spawnEgg), xo + 7 + width, yo);
                }

                if (mouseX >= xo && mouseX <= xo + 25 + width
                    && mouseY >= yo - 1 && mouseY <= yo + 17) {
                    guiGraphics.renderTooltip(font, TextHelper.translatable("entity." + entityId.getNamespace() + "." + entityId.getPath()), xo, yo + 27);
                }

                xo += 27 + width;
            }
        }

        renderSatisfied(guiGraphics, xo, yo, mouseX, mouseY);
    }
}
