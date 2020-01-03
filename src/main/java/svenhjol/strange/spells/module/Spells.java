package svenhjol.strange.spells.module;

import com.google.common.base.CaseFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.charm.Charm;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.spells.client.SpellsClient;
import svenhjol.strange.spells.entity.TargettedSpellEntity;
import svenhjol.strange.spells.item.SpellBookItem;
import svenhjol.strange.spells.spells.Spell;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true)
public class Spells extends MesonModule
{
    public static SpellBookItem book;
    public static EntityType<? extends Entity> entity;
    public static Map<String, Spell> spells = new HashMap<>();

    @Config(name = "Add spell books to loot", description = "If true, common spell books will be added to dungeon loot and rare books to stronghold and vaults.")
    public static boolean addSpellBooksToLoot = true;

    @Config(name = "Enabled spells", description = "List of all available spells.")
    public static List<String> enabledSpells = Arrays.asList(
        "aura",
        "blink",
        "boost",
        "drain",
        "explosion",
        "freeze",
        "growth",
        "heat",
        "knockback",
        "levitate",
        "lightning",
        "portal",
        "repel",
        "rise",
        "roots",
        "slowness",
        "summon",
        "transfer"
    );

    @Config(name = "Common spells", description = "Subset of 'enabled spells' that appear in common dungeon loot and villager trades.")
    public static List<String> commonSpells = Arrays.asList(
        "aura",
        "freeze",
        "growth",
        "heat",
        "knockback",
        "levitate",
        "repel",
        "slowness"
    );

    public static List<String> transferBlacklist = Arrays.asList(
        "minecraft:bedrock",
        "minecraft:end_portal_frame",
        "minecraft:end_portal",
        "minecraft:iron_door",
        "charm:rune_portal_frame",
        "charm:rune_portal"
    );

    public static List<String> transferHeavy = Arrays.asList(
        "minecraft:spawner",
        "minecraft:dragon_egg"
    );

    @OnlyIn(Dist.CLIENT)
    public static SpellsClient client;

    public static BasicParticleType spellParticle;
    public static BasicParticleType enchantParticle;

    @Override
    public void init()
    {
        // init items
        book = new SpellBookItem(this);

        // add the valid spell instances
        for (String id : enabledSpells) {
            String niceName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, id);
            String className = "svenhjol.strange.spells.spells." + niceName + "Spell";

            try {
                Meson.debug("Trying to load spell class " + className);
                Class<?> clazz = Class.forName(className);
                Spell instance = (Spell)clazz.getConstructor().newInstance();
                spells.put(id, instance);
            } catch (Exception e) {
                Meson.warn("Could not load spell " + id, e);
            }
        }

        // register targetted spell entity
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "targetted_spell");
        entity = EntityType.Builder.create(TargettedSpellEntity::new, EntityClassification.MISC)
            .size(1.5F, 1.5F)
            .build(res.getPath())
            .setRegistryName(res);
        RegistryHandler.registerEntity(entity, res);

        ResourceLocation spellRes = new ResourceLocation(Strange.MOD_ID, "spell");
        BasicParticleType spellType = new BasicParticleType(false);
        spellType.setRegistryName(spellRes);
        spellParticle = spellType;

        ResourceLocation enchantRes = new ResourceLocation(Strange.MOD_ID, "enchant");
        BasicParticleType enchantType = new BasicParticleType(false);
        enchantType.setRegistryName(enchantRes);
        enchantParticle = enchantType;

        RegistryHandler.registerParticleType(spellType, spellRes);
        RegistryHandler.registerParticleType(enchantType, enchantRes);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        // spellbooks are valid bookshelf items
        if (Charm.loader.hasModule(BookshelfChests.class)) {
            BookshelfChests.validItems.add(SpellBookItem.class);
        }
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        client = new SpellsClient();
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
            LootEntry entry = ItemLootEntry.builder(Spells.book)
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

    public static void effectEnchant(ServerWorld world, Vec3d vec, Spell spell, int particles, double xOffset, double yOffset, double zOffset, double speed)
    {
        for (int i = 0; i < 1; i++) {
            double px = vec.x;
            double py = vec.y + 1.75D;
            double pz = vec.z;
            world.spawnParticle(Spells.enchantParticle, px, py, pz, particles, xOffset, yOffset, zOffset, speed);
        }
    }

    public static ItemStack attachRandomSpell(ItemStack book, Random rand, boolean useRare)
    {
        List<String> pool = useRare ? enabledSpells : commonSpells;
        String id = pool.get(rand.nextInt(pool.size()));
        Meson.debug("Attaching spell " + id + " to spellbook");
        SpellBookItem.putSpell(book, Spells.spells.get(id));
        return book;
    }
}
