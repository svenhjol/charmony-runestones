package svenhjol.charmony.runestones.client;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.client.features.runestones.Runestones;

public class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Ensure charmony is launched first.
        svenhjol.charmony.core.client.ClientInitializer.init();

        // Launch the mod.
        var mod = RunestonesMod.instance();
        mod.addSidedFeature(Runestones.class);
        mod.run(Side.Client);
    }
}
