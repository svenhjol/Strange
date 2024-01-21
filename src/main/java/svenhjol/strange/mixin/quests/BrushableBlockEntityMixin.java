package svenhjol.strange.mixin.quests;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BrushableBlockEntity.class)
public class BrushableBlockEntityMixin {
    @Redirect(
        method = "unpackLootTable",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;J)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
        )
    )
    private ObjectArrayList<ItemStack> hookUnpackLootTable(LootTable instance, LootParams lootParams, long seed) {
        var list = instance.getRandomItems(lootParams, seed);

        // If additional pool added to an archaeology loot table, select randomly from the pools.
        if (list.size() > 1) {
            var random = RandomSource.create(seed);

            var defaultItem = list.get(0);
            var randomItem = list.get(random.nextInt(list.size()));
            return ObjectArrayList.of(randomItem.isEmpty() ? defaultItem : randomItem);
        }

        return list;
    }
}
