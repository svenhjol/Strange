package svenhjol.strange.feature.quests.reward.function;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionParameters;

public class MakeStructureMap implements RewardItemFunction {
    public static final String ID = "make_structure_map";

    private Parameters params;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RewardItemFunction withParameters(RewardItemFunctionParameters params) {
        this.params = new Parameters(params);
        return this;
    }

    @Override
    public void apply(RewardItem reward) {
        var quest = reward.quest;
        var stack = reward.stack;
        var random = quest.random();

        if (random.nextDouble() < params.chance && stack.is(Items.MAP)) {
            if (quest.player() instanceof ServerPlayer serverPlayer) {
                var pos = serverPlayer.blockPosition();
                var level = (ServerLevel)serverPlayer.level();

                // TODO: this is copypasta from Runestones#tryLocate. Move to helper?
                var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(params.structure);
                if (structure == null) {
                    throw new RuntimeException("Could not get structure from registry: " + params.structure);
                }

                var set = HolderSet.direct(Holder.direct(structure));
                var result = level.getChunkSource().getGenerator().findNearestMapStructure(level, set, pos, 100, true);
                if (result == null) {
                    throw new RuntimeException("Could not locate structure: " + params.structure);
                }

                var targetPos = result.getFirst();
                var mapItem = MapItem.create(level, targetPos.getX(), targetPos.getZ(), (byte) 2, true, true);
                MapItem.renderBiomePreviewMap(level, mapItem);
                MapItemSavedData.addTargetDecoration(mapItem, targetPos, "+", MapDecoration.Type.TARGET_X);
                mapItem.setHoverName(Component.translatable(params.title));
                reward.stack = mapItem;
            }
        }
    }

    public static class Parameters {
        public final double chance;
        public final String title;
        public final ResourceLocation structure;

        public Parameters(RewardItemFunctionParameters params) {
            this.chance = params.getDouble("chance", 1.0d);
            this.title = params.getString("title", "filled_map.strange.default");
            this.structure = params.getResourceLocation("structure", new ResourceLocation("village"));
        }
    }
}
