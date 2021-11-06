package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class JournalBiomeScreen extends JournalScreen {
    private final ResourceLocation biome;

    public JournalBiomeScreen(ResourceLocation biome) {
        super(new TextComponent(StringHelper.snakeToPretty(biome.getPath())));

        // add a back button at the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> biomes(), GO_BACK));

        this.biome = biome;
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, titleX, 16, titleColor);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        if (journal == null) {
            return;
        }

        KnowledgeClient.getKnowledgeData().ifPresent(knowledge -> {
            Optional<String> runes = knowledge.biomes.get(biome);
            if (runes.isEmpty()) return;
            String knownRunes = KnowledgeHelper.convertRunesWithLearnedRunes(runes.get(), journal.getLearnedRunes());

            int left = midX - 74;
            int top = 80;
            int xOffset = 13;
            int yOffset = 15;

            KnowledgeClient.renderRunesString(minecraft, poseStack, knownRunes, left, top, xOffset, yOffset, 12, 3, knownColor, unknownColor, false);
        });
    }
}
