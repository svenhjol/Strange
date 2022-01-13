package svenhjol.strange.module.bookmarks;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import svenhjol.charm.enums.ICharmEnum;

public enum DefaultIcon implements ICharmEnum {
    OVERWORLD(Items.GRASS_BLOCK),
    NETHER(Items.NETHERRACK),
    END(Items.END_STONE),
    DEATH(Items.SKELETON_SKULL);

    private final Item item;

    DefaultIcon(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public ResourceLocation getId() {
        return DefaultedRegistry.ITEM.getKey(item);
    }

    public static DefaultIcon forDimension(ResourceLocation res) {
        if (res.equals(Level.NETHER.location())) {
            return DefaultIcon.NETHER;
        } else if (res.equals(Level.END.location())) {
            return DefaultIcon.END;
        } else {
            return DefaultIcon.OVERWORLD;
        }
    }
}
