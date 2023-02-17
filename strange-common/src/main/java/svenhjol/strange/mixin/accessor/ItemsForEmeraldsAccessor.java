package svenhjol.strange.mixin.accessor;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerTrades.ItemsForEmeralds.class)
public interface ItemsForEmeraldsAccessor {
    @Accessor("itemStack")
    ItemStack getItemStack();
}
