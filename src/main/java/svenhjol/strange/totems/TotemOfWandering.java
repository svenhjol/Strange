package svenhjol.strange.totems;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.VillagerHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.EntityDropsCallback;
import svenhjol.strange.Strange;

import java.util.Random;

@Module(mod = Strange.MOD_ID, description = "A Totem of Wandering lets you quickly teleport to a location in your travel journal.")
public class TotemOfWandering extends CharmModule {
    public static TotemOfWanderingItem TOTEM_OF_WANDERING;
    public static double lootingBoost = 0.25D;

    @Config(name = "Drop chance", description = "Chance (out of 1.0) of a wandering trader dropping a Totem of Wandering when killed by the player.")
    public static double dropChance = 0.25D;

    @Override
    public void register() {
        TOTEM_OF_WANDERING = new TotemOfWanderingItem(this);
    }

    @Override
    public void init() {
        EntityDropsCallback.EVENT.register(this::tryDropTotemFromWanderingTrader);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        VillagerHelper.addWanderingTrade(new TotemOfWanderingForEmeraldsTrade(), false);
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (id.equals(LootTables.PILLAGER_OUTPOST_CHEST)
            || id.equals(LootTables.WOODLAND_MANSION_CHEST)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(UniformLootTableRange.between(0.0F, 1.0F))
                .with(ItemEntry.builder(TOTEM_OF_WANDERING));

            supplier.pool(builder);
        }
    }

    public ActionResult tryDropTotemFromWanderingTrader(LivingEntity entity, DamageSource source, int lootingLevel) {
        if (!entity.world.isClient
            && entity instanceof WanderingTraderEntity
            && source.getAttacker() instanceof PlayerEntity
            && entity.world.random.nextFloat() <= (dropChance + lootingBoost * lootingLevel)

        ) {
            World world = entity.getEntityWorld();
            BlockPos pos = entity.getBlockPos();

            ItemStack totem = new ItemStack(TOTEM_OF_WANDERING);
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), totem));
        }

        return ActionResult.PASS;
    }

    public static class TotemOfWanderingForEmeraldsTrade implements TradeOffers.Factory {
        @Override
        public TradeOffer create(Entity trader, Random rand) {
            if (!trader.world.isClient) {
                ItemStack in = new ItemStack(Items.EMERALD, rand.nextInt(3) + 3);
                ItemStack out = new ItemStack(TOTEM_OF_WANDERING);
                return new TradeOffer(in, out, 4, 5, 0.2F);
            }

            return null;
        }
    }
}
