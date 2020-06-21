## 1.0.0 beta 6
* Fixed totem of returning not being destroyed when used. #74
* Fixed repeating sounds when touching a runestone portal. #81
* Player can now always see the rune diagram page in travel journal. #72
* Opening travel journal pages causes them to lose custom name. #79
* Experimental vanilla locate fix (disabled by default). #75
* Quest reward items with enchantments now show their enchantment on hover. #83
* Quest provided items such as maps and compasses no longer expire after quest is done. #82
* Added `/strange learnrune` and `/strange learnall` commands

NOTE: If you are upgrading from **beta 5**, change the following entry in your `strange-common.toml` config file:
```
[Runestones.Runestones]
    "Available destinations"
        The value "minecraft:jungle_pyramid" changes to "minecraft:jungle_temple".
```
After changing this value, runestones will once more be able to link to Jungle Pyramids.


## 1.0.0 beta 5 hotfix 1
* Fixed crash when holding a travel journal page.

## 1.0.0 beta 5
* Fixed vault chests being empty when disabling totems. Fixes #56
* Added compatiblity with Java 9+
* Added Ender Geodes, Amethyst and Moonstones
* Added Rune Portals
* Added Travel Journal pages for sharing entries.
* Removed old beta 4 portal runestones and obelisks.  This will cause registration warnings in existing worlds.
* Rebuild of Travel Journal client.
* Vaults now spawn under stone circles again.  `strange:vaults` has been deregistered and *may* cause registration warnings or world corruption when upgrading from an old beta.
* Tweak scrollkeeper raids. No longer raids until Journeyman.
* Totem of Returning rewards no longer embed the player under the earth.
* Chunk preloading for teleport via runestones.

## 1.0.0 beta 4
* World hang when doing `/locate` in non-vanilla dimension (1.14 only). Fixes #22
* "Fetch" quests were starting even though the start conditions were invalid. #28
* Improved quest completion text and added co-ordinates for "Fetch" quests. #28
* Fix entry name change not being respected when taking a photo. Fixes #38
* Config option to always show X and Z coordinates of a journal entry in the Travel Journal.
* Vault and Stone Circle biome config has been changed to allow more biomes. If you have customized this config, you may need to look over it.
* Stone Circles may now generate in the End with harvestable Portal Runestones.
* Added RunePortals (documentation Todo!) and Rune Page to the Travel Journal.
* Ambience module has been removed and put in a separate mod Charmonium. This will cause registration warnings in existing worlds.
* [1.15] Totem of Preserving's "drop on death" will be disabled if Quark Oddities is present.

## 1.0.0 beta 3
* Obelisk right click issue. Fixes #26
* Crash when creating travel journal entry at 0 0. Fixes #25
* World hang when doing /locate in non-vanilla dimensions. Fixes #22
* More obvious that you should return to scrollkeeper when quest complete. #28
* Stone Circle maps don't generate anymore due to lag issues. #32
* Added config for ambience volume.

## 1.0.0 beta 2
* Fixed memory leak in travel journal.
* Fixed structure spawning check issue and not obeying config.
* [1.15] Fixed stone circle runestones not taking you to previously discovered location. #17
* Removed stone circles compasses (they were rubbish anyway) to fix a server crash. #15
* Player now receives Resistance instead of Regeneration when using a runestone teleport.
* Behavior of Totem of Preserving has been changed so that you need to be holding a totem for it to preserve items. Configurable. #21
* Restore Quark-based Quests
* Complete rework of obelisks.
* Retexture runestones.
* Testing totem flying issue.

## 1.0.0 beta 1
* Fixed issue with daytime not being reported to client properly.
* Legendary items are now tag based (merge PR#12)
* Update some calls in preparation for 1.15.2
* Depends on Charm 1.5.4

## 1.0.0 alpha 12
* Fixed an issue where scrollkeeper trades are always 2 emeralds. Fix #9
* Fixed rewards given for an incomplete quest when "handing in" a completed one. Fix #10
* Fixed creative mode coordinates text color on travel journal entry.
* Additional delay before reopening journal after taking photo.
* Runestones now give the player a buff during transport. Configurable duration. Test #6

## 1.0.0 alpha 11
* Fixed infinite loading when looking for structure in the wrong dimension
* Fixed issue with Blur mod crashing after taking photo in journal
* Travel journal photos now part of entry rather than separate screen
* Runestone destination algorithm has been changed
* Ambience added for The End
* Obelisks now provide enchanting power equivalent to bookshelves

## 1.0.0 alpha 10
* Fixed rare enchantments being applied outside of vaults
* Fixed totems not being picked properly in loot
* Fixed some broken quests
* Fixed mesa mineshaft ruin pathfinding and loot balance
* Random ore in ruins is now a bit rarer
* Reduce all ambient sound volume
* Swamp ruins have more loot

## 1.0.0 alpha 9
* Loader refactor, code cleanup

## 1.0.0 alpha 8
* Added Obelisks
* Master scrolls no longer found in loot
* Stone circles no longer spawn on Mushroom Islands
* Fixed network message log spamming issue
* Increased distance between stone circles and underground ruins
* Increased outerlands thresholds from 12 million to 15 million blocks

## 1.0.0 alpha 7
* Removed spells, spellbooks and moonstones
* Added legendary items and treasure enchantments - WIP
* Scrollkeepers now change maximum of 2 emeralds for a scroll

## 1.0.0 alpha 6
* Fixed issue with players not being visible to each other in multiplayer
* Fixed stone circles not generating in biomes other than extreme hills
* Remove duplicate spawnpoint runestone
* Added stone markers on surface above underground ruins
* Added compasses that point to stone circles under a full moon
* Rework loader in line with Charm 1.5.2

## 1.0.0 alpha 5
* Added sounds for more biome categories
* Fixed ambient volume and location issues
* Fixed some quest requirements being too difficult
* Corrected some message encoding issues

## 1.0.0 alpha 4
* Experimental client-side ambient sounds. WIP
* Spells are now AOE only
* Restored Totem of Transferring

## 1.0.0 alpha 3
* Changed rune hover text
* Fixed issue with gather quests and magnetic tool

## 1.0.0 alpha 2
* Fixed issue with crafting not giving you the final crafted item
* Fixed some quest reward items not being named correctly

## 1.0.0 alpha 1
* Initial release