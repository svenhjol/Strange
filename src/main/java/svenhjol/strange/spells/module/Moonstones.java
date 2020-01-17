package svenhjol.strange.spells.module;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.spells.item.MoonstoneItem;
import svenhjol.strange.spells.spells.Spell;

import java.util.List;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true, configureEnabled = false,
    description = "")
public class Moonstones extends MesonModule
{
    public static MoonstoneItem item;

    @Config(name = "Add moonstones to loot", description = "If true, enchanted moonstones will be added to dungeon loot, buried treasure and nether fortresses.")
    public static boolean addMoonstonesToLoot = true;

    @Config(name = "Enchantment glint", description = "If true, enchanted moonstones will have the glint effect.")
    public static boolean glint = true;

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && Strange.hasModule(Spells.class);
    }

    @Override
    public void init()
    {
        item = new MoonstoneItem(this);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
        if (!addMoonstonesToLoot) return;

        int weight = 0;
        int quality = 2;
        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_SIMPLE_DUNGEON)) {
            weight = 4;
        } else if (res.equals(LootTables.CHESTS_BURIED_TREASURE)) {
            weight = 6;
        } else if (res.equals(LootTables.CHESTS_NETHER_BRIDGE)) {
            weight = 8;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Moonstones.item)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    Random rand = context.getRandom();
                    return attachRandomSpell(stack, rand);
                })
                .build();

            LootTable table = event.getTable();
            LootHelper.addTableEntry(table, entry);
        }
    }

    public static void effectEnchantStone(ServerPlayerEntity player, Spell spell, int particles, double xOffset, double yOffset, double zOffset, double speed)
    {
        Spells.effectEnchant((ServerWorld)player.world, player.getPositionVec(), spell, particles, xOffset, yOffset, zOffset, speed);
    }

    public static ItemStack attachRandomSpell(ItemStack stone, Random rand)
    {
        List<String> pool = Spells.enabledSpells;
        String id = pool.get(rand.nextInt(pool.size()));
        Meson.debug("Attaching spell " + id + " to moonstone");
        Spell spell = Spells.spells.get(id);

        MoonstoneItem.putSpell(stone, spell);
        Spells.putUses(stone, (int)(spell.getUses() * (1.0 + rand.nextFloat())));
        return stone;
    }
}
