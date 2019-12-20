package svenhjol.strange.spells.module;

import com.google.common.base.CaseFormat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.charm.Charm;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.client.SpellsClient;
import svenhjol.strange.spells.entity.TargettedSpellEntity;
import svenhjol.strange.spells.item.SpellBookItem;
import svenhjol.strange.spells.item.StaffItem;
import svenhjol.strange.spells.spells.Spell;
import svenhjol.strange.spells.spells.Spell.Element;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true)
public class Spells extends MesonModule
{
    public static SpellBookItem book;
    public static EntityType<? extends Entity> entity;
    public static Map<String, Spell> spells = new HashMap<>();
    public static List<Item> staves = new ArrayList<>();

    @Config(name = "Add spell books to loot", description = "If true, common spell books will be added to dungeon loot and rare books to stronghold and vaults.")
    public static boolean addSpellBooksToLoot = true;

    @Config(name = "Lecterns charge staves", description = "If true, placing a spell book on a lectern will transfer the spell to an empty staff held by a nearby player.")
    public static boolean lecternCharging = true;

    @Config(name = "Enabled spells", description = "List of all available spells.")
    public static List<String> enabledSpells = Arrays.asList(
        "aura",
        "blink",
        "boost",
        "drain",
        "explosion",
        "freeze",
        "growth",
        "knockback",
        "pacify",
        "portal",
        "repel",
        "rise",
        "roots",
        "summon",
        "thaw"
    );

    @Config(name = "Common spells", description = "Subset of 'enabled spells' that appear in common dungeon loot and villager trades.")
    public static List<String> commonSpells = Arrays.asList(
        "aura",
        "freeze",
        "growth",
        "knockback",
        "pacify",
        "repel"
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
    public static Map<Element, BasicParticleType> spellParticles = new HashMap<>();
    public static Map<Element, BasicParticleType> enchantParticles = new HashMap<>();

    @Override
    public void init()
    {
        // init items
        book = new SpellBookItem(this);

        staves.add(new StaffItem(this, "wooden", ItemTier.WOOD, 0.5F)
            .setDurationMultiplier(0.65F)
            .setAttackDamage(2.0F));

        staves.add(new StaffItem(this, "stone", ItemTier.STONE, 0.75F)
            .setCapacityMultiplier(1.25F)
            .setAttackDamage(3.0F));

        staves.add(new StaffItem(this, "iron", ItemTier.IRON, 1.0F)
            .setDurationMultiplier(0.85F)
            .setAttackDamage(4.0F));

        staves.add(new StaffItem(this, "golden", ItemTier.GOLD, 0.75F)
            .setDurationMultiplier(0.65F)
            .setCapacityMultiplier(1.25F)
            .setAttackDamage(2.0F));

        staves.add(new StaffItem(this, "diamond", ItemTier.DIAMOND, 0.75F)
            .setAttackDamage(5.0F));

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

        // register spell and enchantment particles
        for (Element el : Element.values()) {
            ResourceLocation spellRes = new ResourceLocation(Strange.MOD_ID, el.getName() + "_spell");
            BasicParticleType spellType = new BasicParticleType(false);
            spellType.setRegistryName(spellRes);
            spellParticles.put(el, spellType);

            ResourceLocation enchantRes = new ResourceLocation(Strange.MOD_ID, el.getName() + "_enchant");
            BasicParticleType enchantType = new BasicParticleType(false);
            enchantType.setRegistryName(enchantRes);
            enchantParticles.put(el, enchantType);

            RegistryHandler.registerParticleType(spellType, spellRes);
            RegistryHandler.registerParticleType(enchantType, enchantRes);
        }
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
    public void onAnvilUpdate(AnvilUpdateEvent event)
    {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        ItemStack out;

        if (left.isEmpty() || right.isEmpty()) return;
        if (!(left.getItem() instanceof StaffItem)) return;
        if (right.getItem() != book) return;

        Spell spell = SpellBookItem.getSpell(right);
        if (spell == null) return;

        int cost = spell.getApplyCost();

        out = left.copy();

        if (StaffItem.hasSpell(out)) {
            StaffItem.clear(out);
        }

        StaffItem.putSpell(out, spell);

        event.setCost(cost);
        event.setMaterialCost(1);
        event.setOutput(out);
    }

    @SubscribeEvent
    public void onOpenContainer(PlayerContainerEvent.Open event)
    {
        if (event.getContainer() instanceof LecternContainer) {
            LecternContainer container = (LecternContainer)event.getContainer();
            ItemStack book = container.getBook();
            if (book.getItem() == Spells.book) {
                event.getPlayer().closeScreen();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent.RightClickBlock event)
    {
        if (!event.getWorld().isRemote
            && event.getPlayer() != null
        ) {
            World world = event.getWorld();
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() == Blocks.LECTERN) {
                PlayerEntity player = event.getPlayer();
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof LecternTileEntity) {
                    LecternTileEntity lectern = (LecternTileEntity)tile;
                    ItemStack book = lectern.getBook();
                    if (book.getItem() == Spells.book) {
                        PlayerHelper.addOrDropStack(player, book);
                        world.setBlockState(pos, state.with(LecternBlock.HAS_BOOK, false), 2);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (lecternCharging
            && event.phase == Phase.END
            && event.player != null
            && !event.player.world.isRemote
            && event.player.world.getGameTime() % 20 == 0
        ) {
            PlayerEntity player = event.player;
            World world = player.world;

            Hand staffHand = null;
            if (player.getHeldItemMainhand().getItem() instanceof StaffItem) {
                staffHand = Hand.MAIN_HAND;
            } else if (player.getHeldItemOffhand().getItem() instanceof StaffItem) {
                staffHand = Hand.OFF_HAND;
            }

            if (staffHand == null) return;
            if (player.world.rand.nextFloat() > 0.5F) return;

            ItemStack staff = player.getHeldItem(staffHand);
            if (StaffItem.hasSpell(staff)) return;

            int[] range = new int[] { 2, 2, 2 };

            BlockPos pos = player.getPosition();
            Stream<BlockPos> inRange = BlockPos.getAllInBox(pos.add(-range[0], -range[1], -range[2]), pos.add(range[0], range[1], range[2]));
            List<BlockPos> blocks = inRange.map(BlockPos::toImmutable).collect(Collectors.toList());
            List<BlockPos> validPositions = new ArrayList<>();

            for (BlockPos blockPos : blocks) {
                if (world.getBlockState(blockPos).getBlock() == Blocks.LECTERN) {
                    validPositions.add(blockPos);
                }
            }

            if (validPositions.isEmpty()) return;
            double dist = 64;
            BlockPos closestPos = null;

            for (BlockPos validPos : validPositions) {
                double between = WorldHelper.getDistanceSq(pos, validPos);
                if (between < dist) {
                    closestPos = validPos;
                    dist = between;
                }
            }
            if (closestPos == null) return;
            Vec3d vec = new Vec3d(closestPos.getX() + 0.5D, closestPos.getY() + 0.5D, closestPos.getZ() + 0.5D);

            TileEntity tile = world.getTileEntity(closestPos);
            if (!(tile instanceof LecternTileEntity)) return;

            LecternTileEntity lectern = (LecternTileEntity)tile;
            ItemStack book = lectern.getBook();
            if (book.getItem() != Spells.book) return;

            Spell spell = SpellBookItem.getSpell(book);
            if (spell == null) return;

            StaffItem.putSpell(staff, spell);
            StaffItem.putUses(staff, 1);
            world.playSound(null, player.getPosition(), StrangeSounds.SPELL_BOOK_CHARGE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            effectEnchantStaff((ServerPlayerEntity)player, spell, 15, 0.1D, 0.4D, 0.1D, 3.0D);
            effectEnchant((ServerWorld)world, vec, spell, 15, 0D, 0.5D, 0D, 4.0D);
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

    public static void effectEnchantStaff(ServerPlayerEntity player, Spell spell, int particles, double xOffset, double yOffset, double zOffset, double speed)
    {
        effectEnchant((ServerWorld)player.world, player.getPositionVec(), spell, particles, xOffset, yOffset, zOffset, speed);
    }

    public static void effectEnchant(ServerWorld world, Vec3d vec, Spell spell, int particles, double xOffset, double yOffset, double zOffset, double speed)
    {
        for (int i = 0; i < 1; i++) {
            double px = vec.x;
            double py = vec.y + 1.75D;
            double pz = vec.z;
            world.spawnParticle(Spells.enchantParticles.get(spell.getElement()), px, py, pz, particles, xOffset, yOffset, zOffset, speed);
        }
    }

    public static ItemStack attachRandomSpell(ItemStack book, Random rand, boolean useRare)
    {
        List<String> pool = useRare ? enabledSpells : commonSpells;
        String id = pool.get(rand.nextInt(pool.size()));
        SpellBookItem.putSpell(book, Spells.spells.get(id));
        return book;
    }
}
