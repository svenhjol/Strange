package svenhjol.strange.module.bookmarks;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
}
