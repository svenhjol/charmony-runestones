package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import java.util.ArrayList;
import java.util.List;

public class Handlers extends Setup<StoneCircles> {
    public Handlers(StoneCircles feature) {
        super(feature);
    }

    /**
     * Loot tables for overworld stone circle archaeology.
     * This is modified by the `excavateRunestonesFromDebris` config.
     *
     * @param original Current definition loot tables.
     * @return List of loot tables for brushable blocks.
     */
    public List<ResourceKey<LootTable>> getOverworldArchaeologyLoot(List<ResourceKey<LootTable>> original) {
        var stoneCircleOverworld = ResourceKey.create(Registries.LOOT_TABLE, RunestonesMod.id("archaeology/stone_circle_overworld"));
        List<ResourceKey<LootTable>> tables = new ArrayList<>(List.of(stoneCircleOverworld));

        // If runestones are enabled and the excavate config is enabled then add a chance of unearthing a runestone.
        if (Mod.getSidedFeature(Runestones.class).enabled() && feature().excavateRunestonesFromDebris()) {
            var stoneCircleRunestone = ResourceKey.create(Registries.LOOT_TABLE, RunestonesMod.id("archaeology/stone_circle_stone_runestone"));
            tables.add(stoneCircleRunestone);
        }

        return tables;
    }

    /**
     * Chance of generating debris around a stone circle.
     * This is modified by the `stoneCircleDebris` config.
     *
     * @param original Current definition debris chance.
     * @return Chance of generation.
     */
    public double getDebrisChance(double original) {
        return feature().stoneCircleDebris() ? original : 0.0d;
    }
}
