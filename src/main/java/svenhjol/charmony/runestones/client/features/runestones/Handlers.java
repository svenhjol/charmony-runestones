package svenhjol.charmony.runestones.client.features.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CActivationWarmup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CTeleportedLocation;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CUniqueWorldSeed;

import java.util.Map;
import java.util.WeakHashMap;

public final class Handlers extends Setup<Runestones> {
    private long seed;
    private boolean hasReceivedSeed = false;

    public final Map<ResourceLocation, String> cachedRunicNames = new WeakHashMap<>();

    public Handlers(Runestones feature) {
        super(feature);
    }

    public void handleTeleportedLocation(S2CTeleportedLocation packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> Minecraft.getInstance()
            .getToastManager()
            .addToast(new TeleportedLocationToast(packet.location())));
    }

    public void handleActivationWarmup(S2CActivationWarmup packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;

            var random = level.getRandom();
            var itemParticle = ParticleTypes.SMOKE;

            var itemDist = 0.1d;
            var itemPos = packet.itemPos();

            for (var i = 0; i < 8; i++) {
                level.addParticle(itemParticle, itemPos.x(), itemPos.y() + 0.36d, itemPos.z(),
                    (itemDist / 2) - (random.nextDouble() * itemDist), 0, (itemDist / 2) - (random.nextDouble() * itemDist));
            }
        });
    }

    public void handleUniqueWorldSeed(S2CUniqueWorldSeed packet, ClientPlayNetworking.Context context) {
        this.seed = packet.seed();
        this.hasReceivedSeed = true;
        feature().handlers.cachedRunicNames.clear();
    }
}
