package svenhjol.strange.spells.module;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.charm.Charm;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.charm.tweaks.module.NoAnvilMinimumXp;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.spells.item.SpellBookItem;

import java.util.List;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true, configureEnabled = false)
public class SpellBooks extends MesonModule
{
    @Config(name = "Add spell books to loot", description = "If true, common spell books will be added to dungeon loot and rare books to stronghold and vaults.")
    public static boolean addSpellBooksToLoot = true;

    @Config(name = "XP repair cost", description = "Amount of levels required to repair a spellbook")
    public static int xpRepairCost = 1;

    public static SpellBookItem book;

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && Strange.hasModule(Spells.class);
    }

    @Override
    public void init()
    {
        // init items
        book = new SpellBookItem(this);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        // spellbooks are valid bookshelf items
        if (Charm.loader.hasModule(BookshelfChests.class)) {
            BookshelfChests.validItems.add(SpellBookItem.class);
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
        if (!addSpellBooksToLoot) return;

        int weight = 0;
        int quality = 2;
        boolean rare = false;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_STRONGHOLD_LIBRARY)) {
            weight = 8;
            rare = true;
        } else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_TEMPLE)) {
            weight = 2;
        } else if (res.equals(LootTables.CHESTS_WOODLAND_MANSION)) {
            weight = 4;
        }

        final boolean useRare = rare;

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(book)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    Random rand = context.getRandom();
                    return attachRandomSpell(stack, rand, useRare);
                })
                .build();

            LootTable table = event.getTable();
            LootHelper.addTableEntry(table, entry);
        }
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event)
    {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        ItemStack out;

        if (left.getItem() instanceof SpellBookItem
            && right.getItem() == Items.PAPER
            && left.getDamage() > 0
        ) {
            out = left.copy();
            int maxDamage = left.getMaxDamage();
            int damage = left.getDamage();
            int repairCost = left.getRepairCost();

            int cost = xpRepairCost == 0 && Charm.hasModule(NoAnvilMinimumXp.class) ? 0 : 1;
            out.setDamage(damage - 2);
            event.setCost(cost);
            event.setMaterialCost(1);
            event.setOutput(out);
        }
    }

    public static ItemStack attachRandomSpell(ItemStack book, Random rand, boolean useRare)
    {
        List<String> pool = useRare ? Spells.enabledSpells : Spells.commonSpells;
        String id = pool.get(rand.nextInt(pool.size()));
        Meson.debug("Attaching spell " + id + " to spellbook");
        SpellBookItem.putSpell(book, Spells.spells.get(id));
        return book;
    }
}
