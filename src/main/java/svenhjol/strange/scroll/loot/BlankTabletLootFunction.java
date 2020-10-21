package svenhjol.strange.scroll.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.module.RunicTablets;

public class BlankTabletLootFunction extends ConditionalLootFunction {

    public BlankTabletLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (!ModuleHandler.enabled("strange:foundations"))
            return stack;

        return tryCreateBlankTablet(stack, context);
    }

    private ItemStack tryCreateBlankTablet(ItemStack stack, LootContext context) {
        if (!RunicTablets.addBlankTabletsToLoot)
            return stack;

        return new ItemStack(RunicTablets.BLANK_TABLET);
    }

    @Override
    public LootFunctionType getType() {
        return RunicTablets.RUNIC_TABLET_LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<BlankTabletLootFunction> {
        @Override
        public BlankTabletLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new BlankTabletLootFunction(conditions);
        }
    }
}
