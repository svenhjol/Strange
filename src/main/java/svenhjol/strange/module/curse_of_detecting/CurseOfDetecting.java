package svenhjol.strange.module.curse_of_detecting;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.EnchantmentsHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@CommonModule(mod = Strange.MOD_ID, description = "An item in your inventory that has Curse of Detecting will cause you to glow and mob awareness to be increased.")
public class CurseOfDetecting extends CharmModule {
    public static CurseOfDetectingEnch CURSE;
    public static final Map<UUID, Boolean> PLAYER_CURSED = new WeakHashMap<>();
    public static final Map<UUID, Long> PLAYER_LAST_CHECK = new WeakHashMap<>();

    public static int checkTicks = 60;
    public static double detectionRange = 32.0D;

    @Override
    public void register() {
        CURSE = new CurseOfDetectingEnch(this);
    }

    public static boolean hasCurse(LivingEntity livingEntity) {
        if (!Strange.LOADER.isEnabled(CurseOfDetecting.class)) return false;
        if (!(livingEntity instanceof Player player)) return false; // only handle players for now

        var uuid = player.getUUID();
        var level = player.getLevel();
        var time = level.getGameTime();
        var hasCurse = (boolean) PLAYER_CURSED.computeIfAbsent(uuid, b -> false);

        if (shouldCheckForCurse(player)) {
            var inventory = player.getInventory();
            var size = inventory.getContainerSize();
            var foundCurse = false;

            for (int i = 0; i < size; i++) {
                var inv = inventory.getItem(i);
                if (inv.isEmpty()) continue;
                if (EnchantmentsHelper.has(inv, CurseOfDetecting.CURSE)) {
                    foundCurse = true;
                    break;
                }
            }

            hasCurse = foundCurse;
            if (hasCurse) {
                var effect = new MobEffectInstance(MobEffects.GLOWING, Math.round(checkTicks * 1.2F));
                livingEntity.addEffect(effect);
            }

            PLAYER_CURSED.put(uuid, hasCurse);
            PLAYER_LAST_CHECK.put(uuid, hasCurse ? time : time + checkTicks);
        }

        return hasCurse;
    }

    public static boolean shouldCheckForCurse(LivingEntity livingEntity) {
        var last = (long)PLAYER_LAST_CHECK.computeIfAbsent(livingEntity.getUUID(), l -> 0L);
        return last == 0 || livingEntity.getLevel().getGameTime() - last > checkTicks;
    }
}
