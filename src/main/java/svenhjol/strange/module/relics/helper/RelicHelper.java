package svenhjol.strange.module.relics.helper;

import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import javax.annotation.Nullable;
import java.util.Random;

public class RelicHelper {
    @Nullable
    public static IRelicItem getRandomItem(Relics.Type type, Random random) {
        if (Relics.RELICS.isEmpty() || !Relics.RELICS.containsKey(type) || Relics.RELICS.get(type).isEmpty()) {
            return null;
        }

        return Relics.RELICS.get(type).get(random.nextInt(Relics.RELICS.get(type).size()));
    }

    public static ItemStack getStackWithDamage(IRelicItem relic, Random random) {
        var stack = relic.getRelicItem();

        if (relic.isDamaged()) {
            int maxDamage = stack.getMaxDamage();
            stack.setDamageValue(Math.max(0, (maxDamage / 2) - random.nextInt(maxDamage / 2)));
        }

        stack.getOrCreateTag().putBoolean(Relics.RELIC_TAG, true);
        return stack;
    }
}
