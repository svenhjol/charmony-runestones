package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import svenhjol.charmony.runestones.RunestonesMod;

public final class LootTables {
    public static final ResourceKey<LootTable> STONE_CIRCLE_OVERWORLD_CHEST
        = ResourceKey.create(Registries.LOOT_TABLE, RunestonesMod.id("chests/stone_circle_overworld"));
    public static final ResourceKey<LootTable> STONE_CIRCLE_OVERWORLD_ARCHAEOLOGY
        = ResourceKey.create(Registries.LOOT_TABLE, RunestonesMod.id("archaeology/stone_circle_overworld"));
    public static final ResourceKey<LootTable> STONE_CIRCLE_STONE_RUNESTONE_ARCHAEOLOGY
        = ResourceKey.create(Registries.LOOT_TABLE, RunestonesMod.id("archaeology/stone_circle_stone_runestone"));
}
