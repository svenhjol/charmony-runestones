# Changelog

## 1.12.2

- Remove music disc from loot.

## 1.12.1

- Update toast text color.

## 1.12.0

- Consolidated namespace.
- Fix hud renderer for runestones.

## 1.11.1

- Fix for 1.21.6.

## 1.11.0

- Update for Charmony and API.

## 1.10.4

- Use WrapOperation rather than Redirect in mixin.

## 1.10.3

- Use the charmony random offset helper.

## 1.10.2

- Refactor loot and tag names for consistency.

## 1.10.1

- Use the charmony item lookup helper.

## 1.10.0

- Update for API 1.21

## 1.9.0

- Update for API 1.20

## 1.8.0

- Update for API 1.18

## 1.7.2

- Bump for new version of rune dictionary.

## 1.7.1

- Advancement for travelling to spawn point is now a "goal" rather than a "challenge".
- Added three new advancements for learning a number of unique runestone words.

## 1.7.0

- Fix runestones facing the same way when placed by the game.
- Add new seed calculation based on runestones position to avoid duplication on the Y axis.
- Add configuration for stone circle debris and to add runestone excavation to debris.
- Add many more sacrifice items for each type of runestone.
- Add chest and campfire generation at center of stone circles.
- Update protection duration to match Charmony default.
- Rebase teleportation on the the Charmony teleporter class.
- Move runestones and stone circle definitions into their own init classes.
- Move API consumer code into the register classes.
- Move advancement language strings into the correct asset folder.
- Custom loot table for stone circle debris in overworld.

## 1.6.1

- Update to use rune dictionary library.

## 1.6.0

- Refactor "RunestoneHelper" to "Helpers".

## 1.5.0

- Update for Minecraft 1.21.5.
- Bump stronghold runestone chance to 20%.

## 1.4.1

- Support 1.21.5-rc1.
- Fix runestones not getting definition data early enough; definitions now loaded at SERVER_STARTING. 

## 1.2.2

- Add configuration for harvestable runestones.
- Add configurable familiarity multiplier.
- Add DataComponent for runestones to hold block entity data.
- Add tooltips for harvested runestones.
- Fix platform creation bug (sealevel in the End is zero)
- Show target coordinates on a discovered runestone.

## 1.2.1

- Add animation for discovered runestones.

## 1.2.0

- Add runestone knowledge to track locations that a player has learned.
- StructureRunestones feature now enabled by default.
- Uses bricks for runestone replacement in trail ruins.

## 1.1.0

- Refactor for Charmony 1.19.0.

## 1.0.3

- Modmenu support.

## 1.0.2

- Add overgrown stone circles.
- Simplify stone circle start generation.
- Lots of tweaks to balance.

## 1.0.1

- Runestone blocks now have random rotation.
- Add location quality when spawning runestones to allow for rarer destinations from specific runestone locations.
- Add strongholds as destination to structure runestones and 1% chance from stone circles.
- Tweak structure runestones weighting.
- Add pillar thickness when spawning stone circle columns.
- Stone circle runestones now have a chance to spawn lower than the top of the column.

## 1.0.0

- Initial port and graphics update.
- Add StructureRunestones.