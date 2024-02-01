package svenhjol.strange.feature.quests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class QuestLootFunction extends LootItemConditionalFunction {
    private final LootContext.EntityTarget entityTarget;
    private final ResourceLocation lootTableId;

    public static final Codec<QuestLootFunction> CODEC = RecordCodecBuilder.create(
        instance -> QuestLootFunction.commonFields(instance)
            .and(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(func -> func.entityTarget))
            .and(ResourceLocation.CODEC.fieldOf("lootTableId").forGetter(func -> func.lootTableId))
            .apply(instance, QuestLootFunction::new));

    protected QuestLootFunction(List<LootItemCondition> conditions, LootContext.EntityTarget entityTarget, ResourceLocation lootTableId) {
        super(conditions);
        this.entityTarget = entityTarget;
        this.lootTableId = lootTableId;
    }

    public QuestLootFunction(ResourceLocation id) {
        this(List.of(), LootContext.EntityTarget.THIS, id);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var random = context.getRandom();

        Player player = null;
        var entityParam = context.getParamOrNull(this.entityTarget.getParam());

        if (entityParam instanceof Player p) {
            player = p;
        }
        if (entityParam instanceof FishingHook f) {
            player = f.getPlayerOwner();
        }

        if (player != null) {
            for (var quest : Quests.getPlayerQuests(player).all()) {
                var result = quest.addToLootTable(lootTableId, random);
                if (result.isPresent()) {
                    return result.get();
                }
            }
        }

        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return Quests.lootFunction.get();
    }
}
