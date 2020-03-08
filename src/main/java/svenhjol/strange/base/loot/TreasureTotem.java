package svenhjol.strange.base.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.module.TreasureTotems;

@SuppressWarnings("unused")
public class TreasureTotem extends LootFunction {
    private TreasureTotem(ILootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context) {
        ItemStack treasure = TreasureTotems.getTreasureItem(context.getRandom());
        return treasure != null ? treasure : stack;
    }

    public static class Builder extends LootFunction.Builder<TreasureTotem.Builder> {
        public Builder() {
        }

        @Override
        protected Builder doCast() {
            return this;
        }

        @Override
        public ILootFunction build() {
            return new TreasureTotem(this.getConditions());
        }
    }

    public static class Serializer extends LootFunction.Serializer<TreasureTotem> {
        public Serializer() {
            super(new ResourceLocation(Strange.MOD_ID, "treasure_totem"), TreasureTotem.class);
        }

        @Override
        public void serialize(JsonObject object, TreasureTotem functionClazz, JsonSerializationContext serializationContext) {
            super.serialize(object, functionClazz, serializationContext);
        }

        @Override
        public TreasureTotem deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn) {
            return new TreasureTotem(conditionsIn);
        }
    }
}
