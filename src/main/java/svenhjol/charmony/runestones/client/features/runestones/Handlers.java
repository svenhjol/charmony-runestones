package svenhjol.charmony.runestones.client.features.runestones;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.common.features.runestones.Knowledge;
import svenhjol.charmony.runestones.common.features.runestones.Networking;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CActivationWarmup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CTeleportedLocation;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CUniqueWorldSeed;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneBlockEntity;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneHelper;

import java.util.Map;
import java.util.WeakHashMap;

public final class Handlers extends Setup<Runestones> {
    private long seed;
    private long lastFamiliarNameCache = 0;
    private boolean hasReceivedSeed = false;
    private Knowledge knowledge;

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

    public void handleKnowledge(Player player, Networking.S2CKnowledge payload) {
        this.knowledge = payload.knowledge();
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

    public boolean isFamiliar(RunestoneLocation location) {
        if (!feature().common.get().feature.familiarity()) {
            return false; // Disabled in config.
        }
        if (knowledge == null) {
            return false; // Hasn't been synced with the server.
        }

        return knowledge.locations().contains(location.id());
    }

    public MutableComponent revealedName(RunestoneBlockEntity runestone) {
        var minecraft = Minecraft.getInstance();
        var location = runestone.location;
        var pos = runestone.getBlockPos();
        var familiarity = isFamiliar(location);

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
        var translated = Component.translatable(RunestoneHelper.localeKey(location)).getString();

        var name = Component.translatable("gui.charmony-runestones.runestone.familiar", translated);
        cachedFamiliarNames.clear();
        cachedFamiliarNames.put(pos, name);
        lastFamiliarNameCache = gameTime;
        return name;
    }
}
