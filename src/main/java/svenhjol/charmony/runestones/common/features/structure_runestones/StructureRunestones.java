package svenhjol.charmony.runestones.common.features.structure_runestones;

import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.RunestonesMod;

@FeatureDefinition(
    side = Side.Common,
    enabledByDefault = false,
    description = "Adds runestones to some vanilla structures."
)
public final class StructureRunestones extends SidedFeature {
    public final Handlers handlers;

    public StructureRunestones(Mod mod) {
        super(mod);
        handlers = new Handlers(this);
    }

    public static StructureRunestones feature() {
        return RunestonesMod.instance().sidedFeature(StructureRunestones.class);
    }
}
