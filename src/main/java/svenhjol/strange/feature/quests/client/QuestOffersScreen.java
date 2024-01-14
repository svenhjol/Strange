package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.QuestResources;
import svenhjol.strange.feature.quests.Quests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestOffersScreen extends Screen {
    protected UUID villagerUuid;
    protected VillagerProfession villagerProfession;
    protected int villagerLevel;
    protected List<BaseQuestRenderer<?>> renderers = new ArrayList<>();
    protected int midX;

    public QuestOffersScreen(UUID villagerUuid, VillagerProfession villagerProfession, int villagerLevel) {
        super(makeTitle(villagerProfession));
        this.villagerUuid = villagerUuid;
        this.villagerProfession = villagerProfession;
        this.villagerLevel = villagerLevel;

        var quests = Quests.VILLAGER_QUESTS.getOrDefault(villagerUuid, List.of());
        for (Quest<?> quest : quests) {
            var renderer = quest.type().makeRenderer(quest);
            renderers.add(renderer);
        }
    }

    @Override
    protected void init() {
        super.init();

        midX = width / 2;
        var yOffset = 40;

        for (BaseQuestRenderer<?> renderer : renderers) {
            renderer.initOffer(this, yOffset);
            yOffset += renderer.getOfferHeight();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Render title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xffffff);

        var yOffset = 40;
        for (int i = 0; i < renderers.size(); i++) {
            var renderer = renderers.get(i);
            renderer.renderOffer(this, guiGraphics, yOffset, mouseX, mouseY);
            yOffset += renderer.getOfferHeight();
        }
    }

    static class AcceptQuestButton extends Button {
        static int WIDTH = 70;
        static int HEIGHT = 20;
        static Component TEXT = QuestResources.ACCEPT_QUEST_BUTTON_TEXT;
        public AcceptQuestButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);

            // If player is at max quests, disable the button and show a tooltip.
            var player = Minecraft.getInstance().player;
            if (player != null && QuestHelper.hasMaxQuests(player)) {
                active = false;
                setTooltip(Tooltip.create(TextHelper.translatable("gui.strange.quests.too_many_quests")));
            }
        }
    }

    static Component makeTitle(VillagerProfession profession) {
        var registry = BuiltInRegistries.VILLAGER_PROFESSION;
        var key = registry.getKey(profession);
        return TextHelper.translatable(QuestResources.QUEST_OFFERS_TITLE_KEY,
            TextHelper.translatable("entity." + key.getNamespace() + ".villager." + key.getPath()));
    }
}
