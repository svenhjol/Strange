package svenhjol.strange.base.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.spells.item.MoonstoneItem;
import svenhjol.strange.spells.item.SpellBookItem;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class AddSpellRandomly extends LootFunction
{
    private final List<Spell> spells;

    public AddSpellRandomly(ILootCondition[] conditions, Collection<Spell> spells)
    {
        super(conditions);
        this.spells = ImmutableList.copyOf(spells);
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context)
    {
        Random rand = context.getRandom();
        Spell spell;

        // don't apply if the module is disabled
        if (!Strange.hasModule(Spells.class)) return stack;

        if (!(stack.getItem() instanceof SpellBookItem || !(stack.getItem() instanceof MoonstoneItem))) {
            Meson.warn("Trying to add a spell to something invalid", stack);
            return stack;
        }

        if (spells.isEmpty()) {
            List<Spell> list = Lists.newArrayList();

            for (String spellId : Spells.spells.keySet()) {
                list.add(Spells.spells.get(spellId));
            }

            if (list.isEmpty()) {
                Meson.warn("Spell list is empty, can't add a spell", stack);
                return stack;
            }

            spell = list.get(rand.nextInt(list.size()));
        } else {
            spell = spells.get(rand.nextInt(spells.size()));
        }

        if (stack.getItem() instanceof SpellBookItem) {
            SpellBookItem.putSpell(stack, spell);
        } else if (stack.getItem() instanceof MoonstoneItem) {
            MoonstoneItem.putSpell(stack, spell);
        }
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<AddSpellRandomly>
    {
        public Serializer()
        {
            super(new ResourceLocation(Strange.MOD_ID, "add_spell_randomly"), AddSpellRandomly.class);
        }

        @Override
        public void serialize(JsonObject object, AddSpellRandomly functionClazz, JsonSerializationContext serializationContext)
        {
            super.serialize(object, functionClazz, serializationContext);
            if (!functionClazz.spells.isEmpty()) {
                JsonArray jsonarray = new JsonArray();
                for (Spell spell : functionClazz.spells) {
                    ResourceLocation res = new ResourceLocation(Strange.MOD_ID, spell.getId());
                    jsonarray.add(new JsonPrimitive(res.toString()));
                }
                object.add("spells", jsonarray);
            }
        }

        @Override
        public AddSpellRandomly deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn)
        {
            List<Spell> list = Lists.newArrayList();
            if (object.has("spells")) {
                for (JsonElement el : JSONUtils.getJsonArray(object, "spells")) {
                    String s = JSONUtils.getString(el, "spell");
                    Spell spell = Spells.spells.getOrDefault(s, null);
                    if (spell == null) {
                        continue;
                    }
                    list.add(spell);
                }
            }
            return new AddSpellRandomly(conditionsIn, list);
        }
    }
}
