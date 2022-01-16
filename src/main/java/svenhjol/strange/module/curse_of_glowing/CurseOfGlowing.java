package svenhjol.strange.module.curse_of_glowing;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.EnchantmentsHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@CommonModule(mod = Strange.MOD_ID, description = "An item with Curse of Glowing causes you to glow and mob awareness to be increased if you have it in your inventory.")
public class CurseOfGlowing extends CharmModule {
    public static CurseOfGlowingEnch ENCHANTMENT;
    public static final Map<UUID, Long> LAST_CHECK = new WeakHashMap<>();

    @Override
    public void register() {
        ENCHANTMENT = new CurseOfGlowingEnch(this);
    }

    public static boolean playerHasCurse(Player player) {
        if (!Charm.LOADER.isEnabled(CurseOfGlowing.class)) return false;

        var uuid = player.getUUID();
        var level = player.getLevel();
        var time = level.getGameTime();
        var last = (long) LAST_CHECK.computeIfAbsent(uuid, l -> time);
        var hasCurse = false;

        if (last == 0 || time - last > 100) {
            var inventory = player.getInventory();
            var size = inventory.getContainerSize();

            for (int i = 0; i < size; i++) {
                var inv = inventory.getItem(i);
                if (EnchantmentsHelper.has(inv, Enchantments.BINDING_CURSE)) {
                    hasCurse = true;
                    break;
                }
            }

            if (hasCurse) {
                LAST_CHECK.put(uuid, level.getGameTime());
            } else {
                LAST_CHECK.put(uuid, level.getGameTime() + 100);
            }
        }

        return hasCurse;
    }
}
