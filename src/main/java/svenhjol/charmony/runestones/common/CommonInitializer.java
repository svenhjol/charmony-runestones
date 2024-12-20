package svenhjol.charmony.runestones.common;

import net.fabricmc.api.ModInitializer;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;
import svenhjol.charmony.runestones.common.features.structure_runestones.StructureRunestones;

public class CommonInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        // Ensure charmony is launched first.
        svenhjol.charmony.core.common.CommonInitializer.init();

        // Launch the mod.
        var mod = RunestonesMod.instance();
        mod.addSidedFeature(Runestones.class);
        mod.addSidedFeature(StructureRunestones.class);
        mod.run(Side.Common);
    }
}
