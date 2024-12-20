package svenhjol.charmony.runestones.common.features.structure_runestones;

import net.minecraft.util.Mth;
import svenhjol.charmony.core.annotations.Configurable;
import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import java.util.function.BooleanSupplier;

@FeatureDefinition(
    side = Side.Common,
    enabledByDefault = false,
    description = """
        Adds runestones to some vanilla structures.
        This makes some small changes to world generation that has a chance to not play well with other mods,
        so this feature is disabled by default."""
)
public final class StructureRunestones extends SidedFeature {
    public final Handlers handlers;

    @Configurable(
        name = "Stronghold chance",
        description = """
            Chance (out of 1.0) of a runestone being added instead of a stone brick block.
            This only applies to the vertical center of a cross-shaped room section.""",
        requireRestart = false
    )
    private static double strongholdChance = 0.25d;

    @Configurable(
        name = "Bastion chance",
        description = """
            Chance (out of 1.0) of a runestone being added instead of a cracked blackstone block.
            This applies to any room or section within a bastion.""",
        requireRestart = false
    )
    private static double bastionChance = 0.02d;

    @Configurable(
        name = "Trail Ruins chance",
        description = """
            Chance (out of 1.0) of a runestone being added instead of a dirt block.
            This applies to any block within a trail ruins structure.""",
        requireRestart = false
    )
    private static double trailRuinsChance = 0.04d;

    public StructureRunestones(Mod mod) {
        super(mod);
        handlers = new Handlers(this);
    }

    @Override
    public BooleanSupplier check() {
        return () -> Runestones.feature().enabled();
    }

    public static StructureRunestones feature() {
        return RunestonesMod.instance().sidedFeature(StructureRunestones.class);
    }

    public double strongholdChance() {
        return Mth.clamp(strongholdChance, 0.0d, 1.0d);
    }

    public double bastionChance() {
        return Mth.clamp(bastionChance, 0.0d, 1.0d);
    }

    public double trailRuinsChance() {
        return Mth.clamp(trailRuinsChance, 0.0d, 1.0d);
    }
}
