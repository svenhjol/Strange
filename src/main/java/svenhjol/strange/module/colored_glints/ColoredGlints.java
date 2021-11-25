package svenhjol.strange.module.colored_glints;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.Arrays;
import java.util.Locale;

@CommonModule(mod = Strange.MOD_ID)
public class ColoredGlints extends CharmModule {
    private static final String DEFAULT_COLOR = "purple";
    public static final String TAG_GLINT = "strange_glint";

    private static String glintColor;
    private static boolean enabled = false;

    @Config(name = "Default glint color", description = "Set the default glint color for all enchanted items.")
    public static String configGlintColor = DyeColor.PURPLE.getName();

    @Override
    public void register() {
        if (Arrays.stream(DyeColor.values()).noneMatch(d -> d.getSerializedName().toLowerCase(Locale.ROOT).equals(configGlintColor))) {
            glintColor = DEFAULT_COLOR;
        } else {
            glintColor = configGlintColor;
        }
    }

    @Override
    public void runWhenEnabled() {
        enabled = true;
    }

    /**
     * Add enchantment glint color directly to the input stack with no sanity checking
     */
    public static void applyColoredGlint(ItemStack stack, String color) {
        stack.getOrCreateTag().putString(TAG_GLINT, color);
    }

    public static boolean hasColoredGlint(ItemStack stack) {
        return stack.getOrCreateTag().contains(TAG_GLINT);
    }

    public static String getColoredGlint(ItemStack stack) {
        if (stack != null && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(TAG_GLINT)) {
                return tag.getString(TAG_GLINT);
            }
        }
        return glintColor;
    }

    public static DyeColor getDefaultGlintColor() {
        return DyeColor.valueOf(glintColor.toUpperCase(Locale.ROOT));
    }

    public static boolean enabled() {
        return enabled;
    }
}