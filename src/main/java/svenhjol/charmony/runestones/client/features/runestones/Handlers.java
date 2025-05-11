package svenhjol.charmony.runestones.client.features.runestones;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import svenhjol.charmony.api.runestones.RunestoneLocation;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.rune_dictionary.client.features.RuneDictionary;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.common.features.runestones.Helpers;
import svenhjol.charmony.runestones.common.features.runestones.Networking;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CActivationWarmup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CTeleportedLocation;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneBlockEntity;

import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("unused")
public final class Handlers extends Setup<Runestones> {
    private long lastFamiliarNameCache = 0;
    private long lastRunicNameCache = 0;

    private final Map<BlockPos, MutableComponent> cachedFamiliarNames = new WeakHashMap<>();
    private final Map<ResourceLocation, String> cachedRunicNames = new WeakHashMap<>();

    public Handlers(Runestones feature) {
        super(feature);
    }

    public void handleTeleportedLocation(Player player, S2CTeleportedLocation payload) {
        Minecraft.getInstance()
            .getToastManager()
            .addToast(new TeleportedLocationToast(payload.location()));
    }

    public void handleActivationWarmup(Player player, S2CActivationWarmup payload) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var random = level.getRandom();
        var itemParticle = ParticleTypes.SMOKE;

        var itemDist = 0.1d;
        var itemPos = payload.itemPos();

        for (var i = 0; i < 8; i++) {
            level.addParticle(itemParticle, itemPos.x(), itemPos.y() + 0.36d, itemPos.z(),
                (itemDist / 2) - (random.nextDouble() * itemDist), 0, (itemDist / 2) - (random.nextDouble() * itemDist));
        }
    }

    public void handleDestroyRunestone(Player player, Networking.S2CDestroyRunestone payload) {
        var pos = payload.runestonePos();
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var x = pos.getX() + 0.5d;
        var y = pos.getY() + 0.5d;
        var z = pos.getZ() + 0.5d;

        for (var i = 0; i < 12; i++) {
            var d = level.random.nextGaussian() * 0.5d;
            var e = level.random.nextGaussian() * 0.5d;
            var f = level.random.nextGaussian() * 0.5d;
            level.addParticle(ParticleTypes.LAVA, x, y, z, d, e, f);
        }
    }

    public void hudRender(LayeredDrawerWrapper drawers) {
        drawers.attachLayerAfter(
            IdentifiedLayer.MISC_OVERLAYS,
            RunestonesMod.id("runestone"),
            ((guiGraphics, deltaTracker) -> feature().registers.hudRenderer.render(guiGraphics, deltaTracker)));
    }

    public void playerTick(Player player) {
        if (player.level().isClientSide()) {
            feature().registers.hudRenderer.tick(player);

            // Clear the runic name cache periodically.
            checkRunicNameCache(player.level());
        }
    }

    public String runicName(RunestoneLocation location) {
        var locationId = location.id();
        if (!cachedRunicNames.containsKey(locationId)) {
            RuneDictionary.feature().handlers
                .getRuneWord(locationId)
                .ifPresent(word -> cachedRunicNames.put(locationId, word));
        }

        return cachedRunicNames.getOrDefault(locationId, "");
    }

    public BlockPos lookingAtBlock(Player player) {
        var cameraPosVec = player.getEyePosition(1.0f);
        var rotationVec = player.getViewVector(1.0f);
        var vec3d = cameraPosVec.add(rotationVec.x * 6, rotationVec.y * 6, rotationVec.z * 6);
        var raycast = player.level().clip(new ClipContext(cameraPosVec, vec3d, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return raycast.getBlockPos();
    }

    public boolean isFamiliar(RunestoneLocation location, Player player) {
        if (!feature().common.get().feature.familiarity()) {
            return false; // Disabled in config.
        }
        return RuneDictionary.feature().handlers.knowsWord(player, location.id());
    }

    public MutableComponent revealName(RunestoneBlockEntity runestone, Player player) {
        var minecraft = Minecraft.getInstance();
        var location = runestone.location;
        var pos = runestone.getBlockPos();
        var familiarity = isFamiliar(location, player);

        if (!familiarity) {
            return Component.translatable("gui.charmony-runestones.runestone.unknown");
        }

        if (minecraft.level == null) {
            throw new RuntimeException("Should not be called when level is not loaded");
        }

        var gameTime = minecraft.level.getGameTime();
        if (cachedFamiliarNames.containsKey(pos) && gameTime < lastFamiliarNameCache + 100) {
            return cachedFamiliarNames.get(pos);
        }

        feature().log().debug("Rebuilding name cache");
        var translated = Component.translatable(Helpers.localeKey(location)).getString();

        var name = Component.translatable("gui.charmony-runestones.runestone.familiar", translated);
        cachedFamiliarNames.clear();
        cachedFamiliarNames.put(pos, name);
        lastFamiliarNameCache = gameTime;
        return name;
    }

    private void checkRunicNameCache(Level level) {
        var gameTime = level.getGameTime();
        if (gameTime > lastRunicNameCache + 500) {
            cachedRunicNames.clear();
            lastRunicNameCache = gameTime;
        }
    }
}
