package svenhjol.strange.totems;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.VillagerHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

import java.util.Random;

@Module(mod = Strange.MOD_ID, description = "A Totem of Wandering lets you quickly teleport to a location in your travel journal.")
public class TotemOfWandering extends CharmModule {
    public static TotemOfWanderingItem TOTEM_OF_WANDERING;

    @Override
    public void register() {
        TOTEM_OF_WANDERING = new TotemOfWanderingItem(this);
    }

    @Override
    public void init() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        VillagerHelper.addWanderingTrade(new TotemOfWanderingForEmeraldsTrade(), false);
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (id.equals(LootTables.PILLAGER_OUTPOST_CHEST) || id.equals(LootTables.WOODLAND_MANSION_CHEST)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(UniformLootNumberProvider.create(0.0F, 1.0F))
                .with(ItemEntry.builder(TOTEM_OF_WANDERING));

            supplier.pool(builder);
        }
    }

    public static class TotemOfWanderingForEmeraldsTrade implements TradeOffers.Factory {
        @Override
        public TradeOffer create(Entity trader, Random rand) {
            if (!trader.world.isClient) {
                ItemStack in = new ItemStack(Items.EMERALD, rand.nextInt(3) + 3);
                ItemStack out = new ItemStack(TOTEM_OF_WANDERING);
                return new TradeOffer(in, out, 1, 5, 0.2F);
            }

            return null;
        }
    }
}
