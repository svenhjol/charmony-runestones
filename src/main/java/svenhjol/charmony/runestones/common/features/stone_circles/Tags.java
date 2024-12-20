package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.charmony.runestones.RunestonesMod;

public final class Tags {
    public static final TagKey<Structure> ON_STONE_CIRCLE_MAPS = TagKey.create(Registries.STRUCTURE,
        RunestonesMod.id("on_stone_circle_maps"));
}
