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
import svenhjol.strange.enchantments.module.TreasureEnchantments;

@SuppressWarnings("unused")
public class TreasureEnchantment extends LootFunction {
    private TreasureEnchantment(ILootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context) {
        ItemStack treasure = TreasureEnchantments.getTreasureItem(context.getRandom());
        return treasure != null ? treasure : stack;
    }

    public static class Builder extends LootFunction.Builder<TreasureEnchantment.Builder> {
        public Builder() {
        }

        @Override
        protected Builder doCast() {
            return this;
        }

        @Override
        public ILootFunction build() {
            return new TreasureEnchantment(this.getConditions());
        }
    }

    public static class Serializer extends LootFunction.Serializer<TreasureEnchantment> {
        public Serializer() {
            super(new ResourceLocation(Strange.MOD_ID, "treasure_enchantment"), TreasureEnchantment.class);
        }

        @Override
        public void serialize(JsonObject object, TreasureEnchantment functionClazz, JsonSerializationContext serializationContext) {
            super.serialize(object, functionClazz, serializationContext);
        }

        @Override
        public TreasureEnchantment deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn) {
            return new TreasureEnchantment(conditionsIn);
        }
    }
}
