package svenhjol.strange.module.cooking_pots;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.cooking_pots.network.ServerSendAddToPot;

import java.util.ArrayList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class CookingPots extends CharmModule {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "cooking_pot");

    public static final ResourceLocation TRIGGER_LIT_FIRE = new ResourceLocation(Strange.MOD_ID, "lit_fire_under_pot");
    public static final ResourceLocation TRIGGER_FILLED_WATER = new ResourceLocation(Strange.MOD_ID, "filled_pot_with_water");
    public static final ResourceLocation TRIGGER_ADDED_ITEM = new ResourceLocation(Strange.MOD_ID, "added_item_to_pot");
    public static final ResourceLocation TRIGGER_TAKEN_FOOD = new ResourceLocation(Strange.MOD_ID, "taken_food_from_pot");

    public static CookingPotBlock COOKING_POT;
    public static BlockEntityType<CookingPotBlockEntity> BLOCK_ENTITY;
    public static MixedStewItem MIXED_STEW;

    @Config(name = "Maximum bowls", description = "Maximum number of bowls a cooking pot can hold.")
    public static int maxPortions = 16;

    @Config(name = "Hunger restored", description = "Maximum number of hunger points a mixed stew will restore. Player maximum is 20.")
    public static int hungerRestored = 10;

    @Config(name = "Add food effects", description = "If true, foods that have effects such as hunger or poison have a chance of being added to the pot.")
    public static boolean addFoodEffects = true;

    public static ServerSendAddToPot SERVER_SEND_ADD_TO_POT;

    @Override
    public void register() {
        COOKING_POT = new CookingPotBlock(this);
        BLOCK_ENTITY = CommonRegistry.blockEntity(ID, CookingPotBlockEntity::new, COOKING_POT);
        MIXED_STEW = new MixedStewItem(this);
    }

    @Override
    public void runWhenEnabled() {
        SERVER_SEND_ADD_TO_POT = new ServerSendAddToPot();
    }

    public static List<Item> getResolvedItems(List<ResourceLocation> ids) {
        List<Item> items = new ArrayList<>();

        for (ResourceLocation id : ids) {
            Registry.ITEM.getOptional(id).ifPresent(items::add);
        }

        return items;
    }

    public static void triggerLitFire(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_LIT_FIRE);
    }

    public static void triggerFilledWater(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_FILLED_WATER);
    }

    public static void triggerAddedItem(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_ADDED_ITEM);
    }

    public static void triggerTakenFood(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_TAKEN_FOOD);
    }
}
