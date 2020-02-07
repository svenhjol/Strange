## Final QA / Testing
- [x] Get staves working (holding books, casting etc)
- [x] Wooden staff texture
- [x] Open spellbook texture
- [x] Moonstone and staff recipes
- [x] Growth should grow trees
- [x] Spell book textures
- [x] Check spell lectern drops
- [x] Add graphics and blockstates for all 16 runestones
- [x] Rune decoration uses correct dimension runes
- [x] Split out vaults into separate structure type and make them spawn in extreme hills
- [x] Pillager Outposts will lead you to vaults
- [x] Let vault variants appear in innerlands to avoid massive chunk searching
- [x] Stone circles shouldn't spawn close to spawn point
- [x] Underground Ruins should blacklist big dungeons
- [x] Allow stone circles in all biomes but restrict on first check
- [x] Specify what "air" certain biome ruins should fill with; deprecate ocean checker
- [x] Nether stone circles should be rarer or not have outerlands runes
- [x] Some stone circle maps are pointless, especially those in stone circle chests
- [x] Sound effect for transfer spell is wrong, should not use powerup
- [x] Check player caps are flushed between worlds
- [x] Nudge vaults max vertical down
- [x] Don't restrict new music to Outerlands
- [x] 12 runestones for "inner lands", 4 for "outer lands"
- [x] Fix bookshelf randomness, shouldn't be positive column
- [x] Blacklist vaults for underground ruins
- [x] Freeze spell should make snow appear on solid blocks and slow hostiles
- [x] Heat spell should ignite hostiles and ignite campfires
- [x] Efficiency and Fortune on staff
- [x] "Undir" should play when in Nether < lava ocean
- [x] TEST Add spawner type (for specialised ruins)
- [x] Opening quests in the nether that look for distant vaults causes infinite badness
- [x] Bound compass has error on frame 16
- [x] Encounter boss bar doesn't clear when you die
- [x] Test change to `LivingDeathEvent` code
- [x] IN PROGRESS Restore advancements

## Intermittent / warnings
- [ ] Sometimes scroll doesn't disappear from inventory when accepting / quitting
- [ ] Weird "air_block" warnings sometimes when loading / "bigdungeon not found" warnings
- [ ] the "17" warning
- [x] TEST "strange:module_enabled" error
- [ ] "POI data mismatch" error (java.lang.IllegalStateException: POI data mismatch: already registered at (pos))

## Ruins
- [x] Jungle chambers less glass
- [x] Fix ladders and lanterns
- [x] (Chambers: Done) Jungle, Plains, Forest, Icy, Forest, Ocean, Swamp, Desert, Taiga
- [x] Mesa mineshafts
- [x] Bambi1 -> Extreme Hills
- [x] Forest dungeon
- [x] Forest large tree structure
- [ ] Swamp ruins don't have much loot at endpoints
- [x] Add larger room for variation in "Undir"
- [x] Chambers for Savanna

## Quests
- [x] Move all lang strings into quests
- [x] IN PROGRESS - MORE T5 - Add some new quests
- [x] Handle `"description": "String1 | String2 | String3"`
- [x] Handle output of `"minecraft:iron_pickaxe[enchanted]"`
- [x] Handle `"modules": ["quark:thing_enabled"]`,
- [x] Handle `"modules": ["charm:bookshelf_chests"]`
- [x] Handle input/output of `"minecraft:thing1 | minecraft:thing2"`
- [x] Handle output of `[common], [uncommon], [epic], [rare]`
- [x] Handle `"hint"`
- [x] Handle `"provide": {}` to give quest items on start
- [x] TEST Handle `"ScrollTier1"` (etc)
- [x] Handle values of "x-y"
- [x] TEST Luck should increase scroll value
- [x] Handle "StoneCircleMap" - gen map randomly away from villager
- [x] TEST Don't modify values with an exclamation mark

## Docs
- [x] Update all docs