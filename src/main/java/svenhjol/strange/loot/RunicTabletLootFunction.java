package svenhjol.strange.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.strange.item.RunicTabletItem;
import svenhjol.strange.module.Foundations;
import svenhjol.strange.module.RunicTablets;

public class RunicTabletLootFunction extends ConditionalLootFunction {

    public RunicTabletLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (!ModuleHandler.enabled("strange:foundations"))
            return stack;

        return tryCreateRunicTablet(stack, context);
    }

    private ItemStack tryCreateRunicTablet(ItemStack stack, LootContext context) {
        if (!RunicTablets.addRunicTabletsToLoot)
            return stack;

        if (!DimensionHelper.isOverworld(context.getWorld()))
            return stack;

        Vec3d origin = context.get(LootContextParameters.ORIGIN);
        if (origin == null)
            return stack;

        BlockPos originPos = new BlockPos(origin);
        BlockPos pos = PosHelper.addRandomOffset(originPos, context.getRandom(), 6000);
        BlockPos structurePos = context.getWorld().locateStructure(Foundations.FEATURE, pos, 1500, false);

        if (structurePos == null)
            return stack;

        ItemStack tablet = new ItemStack(RunicTablets.RUNIC_TABLET);
        RunicTabletItem.setDimension(tablet, ServerWorld.OVERWORLD.getValue());
        RunicTabletItem.setPos(tablet, structurePos);
        tablet.setCustomName(new TranslatableText("item.strange.runic_tablet_foundation"));

        return tablet;
    }

    @Override
    public LootFunctionType getType() {
        return RunicTablets.RUNIC_TABLET_LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<RunicTabletLootFunction> {
        @Override
        public RunicTabletLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new RunicTabletLootFunction(conditions);
        }
    }
}
