package svenhjol.strange.runicaltars;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.math.Vec3d;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DimensionHelper;

import java.util.Random;

public class RunicFragmentLootFunction extends ConditionalLootFunction {

    public RunicFragmentLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (!ModuleHandler.enabled("strange:foundations"))
            return stack;

        return tryCreateRunicFragment(stack, context);
    }

    private ItemStack tryCreateRunicFragment(ItemStack stack, LootContext context) {
        if (!DimensionHelper.isOverworld(context.getWorld()))
            return stack;

        Random random = context.getRandom();
        Vec3d origin = context.get(LootContextParameters.ORIGIN);
        if (origin == null)
            return stack;

        if (random.nextFloat() > RunicAltars.lootChance)
            return stack;

        ItemStack fragment = new ItemStack(RunicAltars.RUNIC_FRAGMENT);


        return fragment;
    }

    @Override
    public LootFunctionType getType() {
        return RunicAltars.LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<RunicFragmentLootFunction> {
        @Override
        public RunicFragmentLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new RunicFragmentLootFunction(conditions);
        }
    }
}
