package svenhjol.strange.feature.quests.hunt;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.quests.hunt.HuntQuest.HuntTarget;

public class HuntQuestRenderer extends BaseQuestRenderer<HuntQuest> {
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
        return QuestsResources.HUNT_REQUIREMENT_TEXT;
    }

    @Override
    public Component getRewardText() {
        return QuestsResources.HUNT_REWARD_TEXT;
    }

    @Override
    public ItemStack getQuestIcon() {
        return new ItemStack(Items.CROSSBOW);
    }

    @Override
    protected void renderRequirements(GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;

        var xo = midX + xOffset;
        var yo = yOffset;

        for (var requirement : quest.requirements()) {
            if (requirement instanceof HuntTarget i) {
                Component label;
                if (showRemaining) {
                    label = TextHelper.translatable("gui.strange.quests.remaining", Math.max(0, i.total() - i.remaining()), i.total());
                } else {
                    label = TextHelper.translatable("gui.strange.quests.amount", i.total());
                }

                int width;
                ResourceLocation sprite;
                var entityId = BuiltInRegistries.ENTITY_TYPE.getKey(i.entity);
                var tooltip = TextHelper.translatable("entity." + entityId.getNamespace() + "." + entityId.getPath());

                switch (screenType) {
                    case PAGED_ACTIVE, SELECTED_ACTIVE:
                        guiGraphics.drawString(font, label, xo + 4, yo + 20, i.satisfied() ? satisfiedColor : requirementColor, false);
                        width = font.width(label);

                        guiGraphics.fill(xo, yo - 2, xo + width + 6, yo + 30, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);

                        sprite = QuestsResources.MOB_SPRITES.get(i.entity);
                        if (sprite != null) {
                            guiGraphics.blitSprite(sprite, xo + (width / 2) - 5, yo, 16, 16);
                        } else {
                            ItemLike spawnEgg = SpawnEggItem.byId(i.entity);
                            if (spawnEgg == null) {
                                spawnEgg = Items.ZOMBIE_SPAWN_EGG;
                            }
                            guiGraphics.renderFakeItem(new ItemStack(spawnEgg), xo + (width / 2) + 10, yo);
                        }

                        if (mouseX >= xo + (width / 2) - 5 && mouseX <= xo + (width / 2) + 10
                            && mouseY >= yo && mouseY <= yo + 16) {
                            guiGraphics.renderTooltip(font, tooltip, xo, yo + 27);
                        }

                        xo += 8 + width;
                        break;

                    case PAGED_OFFER:
                        guiGraphics.drawString(font, label, xo + 4, yo + 4, i.satisfied() ? satisfiedColor : requirementColor, false);
                        width = font.width(label);

                        guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);

                        sprite = QuestsResources.MOB_SPRITES.get(i.entity);
                        if (sprite != null) {
                            guiGraphics.blitSprite(sprite, xo + 7 + width, yo, 16, 16);
                        } else {
                            ItemLike spawnEgg = SpawnEggItem.byId(i.entity);
                            if (spawnEgg == null) {
                                spawnEgg = Items.ZOMBIE_SPAWN_EGG;
                            }
                            guiGraphics.renderFakeItem(new ItemStack(spawnEgg), xo + 7 + width, yo);
                        }

                        if (mouseX >= xo && mouseX <= xo + 25 + width
                            && mouseY >= yo - 1 && mouseY <= yo + 17) {
                            guiGraphics.renderTooltip(font, tooltip, xo, yo + 27);
                        }

                        xo += 27 + width;
                        break;
                }
            }
        }
    }
}
