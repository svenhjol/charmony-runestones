package svenhjol.charmony.runestones.client.features.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.client.ClientRegistry;
import svenhjol.charmony.core.events.PlayerTickCallback;
import svenhjol.charmony.runestones.common.features.runestones.Networking.*;

import java.util.ArrayList;
import java.util.Collections;

public final class Registers extends Setup<Runestones> {
    private static final ResourceLocation ILLAGER_GLYPHS = ResourceLocation.withDefaultNamespace("illageralt");

    public final Style runeFont;
    public final HudRenderer hudRenderer;

    public Registers(Runestones feature) {
        super(feature);

        runeFont = Style.EMPTY.withFont(ILLAGER_GLYPHS);
        hudRenderer = new HudRenderer();

        // Handle packets being sent from the server.
        ClientPlayNetworking.registerGlobalReceiver(S2CTeleportedLocation.TYPE,
            feature.handlers::handleTeleportedLocation);
        ClientPlayNetworking.registerGlobalReceiver(S2CActivationWarmup.TYPE,
            feature.handlers::handleActivationWarmup);
        ClientPlayNetworking.registerGlobalReceiver(S2CUniqueWorldSeed.TYPE,
            feature.handlers::handleUniqueWorldSeed);
    }

    @Override
    public Runnable boot() {
        return () -> {
            HudRenderCallback.EVENT.register(feature().handlers::hudRender);
            PlayerTickCallback.EVENT.register(feature().handlers::playerTick);

            var registry = ClientRegistry.forFeature(feature());
            var blockItems = new ArrayList<>(feature().common.get().registers.blockItems);
            Collections.reverse(blockItems);

            for (var blockItem : blockItems) {
                registry.itemTab(
                    blockItem.get(),
                    CreativeModeTabs.FUNCTIONAL_BLOCKS,
                    Items.LODESTONE
                );
            }
        };
    }
}
