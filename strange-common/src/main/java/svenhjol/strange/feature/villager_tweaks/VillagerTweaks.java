package svenhjol.strange.feature.villager_tweaks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import svenhjol.charm_api.event.EntityJoinEvent;
import svenhjol.charm_core.annotation.Configurable;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.fabric.ConfigHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.mixin.accessor.ItemsForEmeraldsAccessor;

import java.util.List;

@Feature(mod = Strange.MOD_ID, description = "Villager changes and nerfs. Opinionated config options are disabled by default.")
public class VillagerTweaks extends CharmFeature {
    public static final List<EntityType<?>> NO_TRAMPLING = List.of(
        EntityType.VILLAGER,
        EntityType.WANDERING_TRADER
    );

    private static boolean hasConflictingMods;

    @Configurable(name = "No cured discount", description = "If true, the trade discount applied when a villager is cured from zombification is removed. " +
        "This feature is disabled if villagerfix is present because it's better.")
    public static boolean noCuredDiscount = false;

    @Configurable(name = "No nitwits", description = "If true, a nitwit spawns as an unemployed villager.")
    public static boolean noNitwits = true;

    @Configurable(name = "No crop trampling", description = "If true, villagers and wandering traders will not trample crops.")
    public static boolean noCropTrampling = true;

    @Configurable(name = "No treasure enchantments", description = "If true, treasure enchantments such as Mending and Infinity are no longer sold by librarians.")
    public static boolean noTreasureEnchantments = false;

    @Configurable(name = "No infinite emeralds", description = "If true, some trades that can result in infinite emerald trading are removed.")
    public static boolean noInfiniteEmeralds = false;

    @Override
    public void register() {
        hasConflictingMods = ConfigHelper.isModLoaded("villagerfix")
            || ConfigHelper.isModLoaded("villagerconfig");

        EntityJoinEvent.INSTANCE.handle(this::handleEntityJoin);

        if (noInfiniteEmeralds) {
            removeVillagerTrades();
        }
    }

    @Override
    public void runWhenEnabled() {
        removeVillagerTrades();
    }

    public static boolean shouldRemoveZombieDiscount() {
        if (noCuredDiscount && !hasConflictingMods) {
            Strange.LOG.debug(VillagerTweaks.class, "Disabling zombie discount for villager");
            return true;
        }

        return false;
    }

    public static boolean shouldNotTrade(Enchantment enchantment) {
        if (noTreasureEnchantments && enchantment.isTreasureOnly()) {
            Strange.LOG.debug(VillagerTweaks.class, "Disabling treasure trade " + enchantment);
            return true;
        }

        return false;
    }

    private void handleEntityJoin(Entity entity, Level level) {
        if (noNitwits) {
            changeNitwitProfession(entity);
        }
    }

    private void removeVillagerTrades() {
        Strange.REGISTRY.removeVillagerTrade(VillagerProfession.LIBRARIAN, 3, listing
            -> listing instanceof VillagerTrades.ItemsForEmeralds trade
            && ((ItemsForEmeraldsAccessor)trade).getItemStack() != null
            && ((ItemsForEmeraldsAccessor)trade).getItemStack().is(Items.GLASS));
    }

    private void changeNitwitProfession(Entity entity) {
        if (!entity.level.isClientSide() && entity instanceof Villager villager) {
            var data = villager.getVillagerData();

            if (data.getProfession() == VillagerProfession.NITWIT) {
                villager.setVillagerData(data.setProfession(VillagerProfession.NONE));
                Strange.LOG.debug(getClass(), "Made nitwit " + villager.getStringUUID() + " into an unemploted villager at pos: " + villager.blockPosition());
            }
        }
    }
}
