package svenhjol.strange.module.relics.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.module.relics.Relics;
import svenhjol.strange.module.relics.helper.RelicHelper;

import java.util.List;

public class RelicLootFunction extends LootItemConditionalFunction {
    public RelicLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var random = context.getRandom();
        List<Relics.Type> types;
        Relics.Type type;

        if (Relics.allowWeirdRelics) {
            types = Relics.Type.getTypes();
            type = types.get(random.nextInt(types.size()));

            // Balance selection to favour weapons.
            if (type.equals(Relics.Type.WEIRD) && random.nextFloat() < 0.7F) {
                type = Relics.Type.WEAPON;
            }
        } else {
            types = Relics.Type.getTypesWithout(Relics.Type.WEIRD);
            type = types.get(random.nextInt(types.size()));
        }

        var relic = RelicHelper.getRandomItem(type, random);
        if (relic == null) return stack;

        return RelicHelper.getStackWithDamage(relic, random);
    }

    @Override
    public LootItemFunctionType getType() {
        return Relics.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RelicLootFunction> {

        @Override
        public RelicLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new RelicLootFunction(conditions);
        }
    }
}
