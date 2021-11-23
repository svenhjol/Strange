package svenhjol.strange.module.experience_bottles;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.rubble.Rubble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID)
public class ExperienceBottles extends CharmModule {
    public static Map<Type, ExperienceBottleItem> EXPERIENCE_BOTTLES = new HashMap<>();
    public static EntityType<ExperienceBottleProjectile> EXPERIENCE_BOTTLE;
    public static ResourceLocation ENTITY_ID = new ResourceLocation(Strange.MOD_ID, "experience_bottle");
    public static ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "experience_bottles_loot");
    public static LootItemFunctionType LOOT_FUNCTION;

    private final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();

    @Override
    public void register() {
        // register each type of experience bottle
        for (Type type : Type.values()) {
            EXPERIENCE_BOTTLES.put(type, new ExperienceBottleItem(this, type));
        }

        // create and register the entity
        EXPERIENCE_BOTTLE = RegistryHelper.entity(ENTITY_ID, FabricEntityTypeBuilder
            .<ExperienceBottleProjectile>create(MobCategory.MISC, ExperienceBottleProjectile::new)
            .trackRangeChunks(4)
            .trackedUpdateRate(10)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)));

        // register loot function
        LOOT_FUNCTION = RegistryHelper.lootFunctionType(LOOT_ID, new LootItemFunctionType(new ExperienceBottleLootFunction.Serializer()));

        // register dispenser behavior
        for (ExperienceBottleItem bottle : EXPERIENCE_BOTTLES.values()) {
            DispenserBlock.registerBehavior(bottle, new AbstractProjectileDispenseBehavior() {
                @Override
                protected Projectile getProjectile(Level level, Position pos, ItemStack itemStack) {
                    return Util.make(new ExperienceBottleProjectile(level, pos.x(), pos.y(), pos.z()), e -> e.setItem(itemStack));
                }
            });
        }
    }

    @Override
    public void runWhenEnabled() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        VALID_LOOT_TABLES.add(Rubble.LOOT);
    }

    private void handleLootTables(ResourceManager manager, LootTables lootTables, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.AIR)
                    .setWeight(1)
                    .apply(() -> new ExperienceBottleLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }

    public enum Type implements ICharmEnum {
        GREATER(1200, Rarity.UNCOMMON, DyeColor.LIME),
        GREATEST(12000, Rarity.RARE, DyeColor.BLUE);

        private final int orbs;
        private final Rarity rarity;
        private final DyeColor color;

        Type(int orbs, Rarity rarity, DyeColor color) {
            this.orbs = orbs;
            this.rarity = rarity;
            this.color = color;
        }

        public int getOrbs() {
            return orbs;
        }

        public Rarity getRarity() {
            return rarity;
        }

        public DyeColor getColor() {
            return color;
        }
    }
}
