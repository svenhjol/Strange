package svenhjol.strange.module.journals.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public abstract class JournalResourceScreen extends JournalScreen {
    private final ResourceLocation resource;

    public JournalResourceScreen(ResourceLocation resource) {
        super(new TextComponent(StringHelper.snakeToPretty(resource.getPath(), true)));
        this.resource = resource;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        if (journal == null) {
            return;
        }

        KnowledgeClient.getKnowledgeData().ifPresent(knowledge -> {
            Optional<String> runes = getBranch(knowledge).get(resource);
            if (runes.isEmpty()) return;
            String knownRunes = KnowledgeHelper.convertRunesWithLearnedRunes(runes.get(), journal.getLearnedRunes());

            int left = midX - 74;
            int top = 80;
            int xOffset = 13;
            int yOffset = 15;

            KnowledgeClient.renderRunesString(minecraft, poseStack, knownRunes, left, top, xOffset, yOffset, 12, 3, knownColor, unknownColor, false);
        });
    }

    public abstract KnowledgeBranch<?, ResourceLocation> getBranch(KnowledgeData knowledge);
}
