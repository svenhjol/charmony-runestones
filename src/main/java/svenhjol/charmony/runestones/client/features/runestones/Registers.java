package svenhjol.charmony.runestones.client.features.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.*;

public final class Registers extends Setup<Runestones> {
    public Registers(Runestones feature) {
        super(feature);

        // Handle packets being sent from the server.
        ClientPlayNetworking.registerGlobalReceiver(S2CTeleportedLocation.TYPE,
            feature.handlers::handleTeleportedLocation);
        ClientPlayNetworking.registerGlobalReceiver(S2CActivationWarmup.TYPE,
            feature.handlers::handleActivationWarmup);
        ClientPlayNetworking.registerGlobalReceiver(S2CUniqueWorldSeed.TYPE,
            feature.handlers::handleUniqueWorldSeed);
    }
}
