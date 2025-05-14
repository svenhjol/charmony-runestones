package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootTable;
import svenhjol.charmony.runestones.RunestonesMod;

public final class Tags {
    public static final TagKey<Structure> ON_STONE_CIRCLE_MAPS = TagKey.create(Registries.STRUCTURE,
        RunestonesMod.id("on_stone_circle_maps"));
    
    public static final ResourceKey<LootTable> STONE_CIRCLE_OVERWORLD_CHEST = ResourceKey.create(Registries.LOOT_TABLE,
        RunestonesMod.id("chest/stone_circle_overworld"));

    public static final ResourceKey<LootTable> STONE_CIRCLE_OVERWORLD_ARCHAEOLOGY = ResourceKey.create(Registries.LOOT_TABLE,
        RunestonesMod.id("archaeology/stone_circle_overworld"));

    public static final ResourceKey<LootTable> STONE_CIRCLE_STONE_RUNESTONE_ARCHAEOLOGY = ResourceKey.create(Registries.LOOT_TABLE,
        RunestonesMod.id("archaeology/stone_circle_stone_runestone"));
}
