package svenhjol.charmony.runestones.client.features.runestones;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.common.features.runestones.Networking;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CActivationWarmup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CTeleportedLocation;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CUniqueWorldSeed;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneHelper;

import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("unused")
public final class Handlers extends Setup<Runestones> {
    private long seed;
    private boolean hasReceivedSeed = false;

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

    public void handleUniqueWorldSeed(Player player, S2CUniqueWorldSeed payload) {
        this.seed = payload.seed();
        this.hasReceivedSeed = true;
        feature().handlers.cachedRunicNames.clear();
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

    public void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        feature().registers.hudRenderer.render(guiGraphics, deltaTracker);
    }

    public void playerTick(Player player) {
        if (player.level().isClientSide()) {
            feature().registers.hudRenderer.tick(player);
        }
    }

    public String runicName(RunestoneLocation location) {
        if (!hasReceivedSeed) {
            throw new RuntimeException("Client has not received the unique world seed");
        }

        var id = location.id();
        if (!cachedRunicNames.containsKey(id)) {
            cachedRunicNames.put(id, RunestoneHelper.generateRunes(location, seed, 12));
        }

        return cachedRunicNames.get(id);
    }

    public BlockPos lookingAtBlock(Player player) {
        var cameraPosVec = player.getEyePosition(1.0f);
        var rotationVec = player.getViewVector(1.0f);
        var vec3d = cameraPosVec.add(rotationVec.x * 6, rotationVec.y * 6, rotationVec.z * 6);
        var raycast = player.level().clip(new ClipContext(cameraPosVec, vec3d, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return raycast.getBlockPos();
    }
}
