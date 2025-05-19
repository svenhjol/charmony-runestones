package svenhjol.charmony.runestones.common.features.structure_runestones;

import net.minecraft.util.Mth;
import svenhjol.charmony.api.core.Configurable;
import svenhjol.charmony.api.core.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.api.core.Side;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import java.util.function.BooleanSupplier;

@FeatureDefinition(
    side = Side.Common,
    description = """
        Adds runestones to some vanilla structures.
        This makes some small changes to world generation that has a chance to not play well with other mods,
        so disable this feature if you run into problems."""
)
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class StructureRunestones extends SidedFeature {
    public final Handlers handlers;

    @Configurable(
        name = "Stronghold chance",
        description = """
            Chance (out of 1.0) of a runestone being added instead of a stone brick block
            within a stronghold room crossing section. Only blocks at a height of 2 are modified.""",
        requireRestart = false
    )
    private static double strongholdChance = 0.2d;

    @Configurable(
        name = "Bastion chance",
        description = """
            Chance (out of 1.0) of a runestone being added instead of a chiseled blackstone block.
            This applies to any room or section within a bastion.""",
        requireRestart = false
    )
    private static double bastionChance = 0.25d;

    @Configurable(
        name = "Trail Ruins chance",
        description = """
            Chance (out of 1.0) of a runestone being added instead of a bricks block.
            This applies to any block within a trail ruins structure.""",
        requireRestart = false
    )
    private static double trailRuinsChance = 0.05d;

    public StructureRunestones(Mod mod) {
        super(mod);
        handlers = new Handlers(this);
    }

    @Override
    public BooleanSupplier check() {
        return () -> Runestones.feature().enabled();
    }

    public static StructureRunestones feature() {
        return Mod.getSidedFeature(StructureRunestones.class);
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
