package svenhjol.charmony.runestones.client.features.runestones;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.common.features.runestones.Helpers;
import svenhjol.charmony.runestones.common.features.runestones.Knowledge;
import svenhjol.charmony.runestones.common.features.runestones.Networking;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CActivationWarmup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CTeleportedLocation;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CUniqueWorldSeed;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneBlockEntity;

import java.util.Map;
import java.util.WeakHashMap;

public final class Handlers extends Setup<Runestones> {
    private static final Character UNKNOWN_LETTER = '?';

    private long seed;
    private boolean hasReceivedSeed = false;
    private Knowledge knowledge;
    private long lastNameCache = 0;
    private final Map<BlockPos, MutableComponent> nameCache = new WeakHashMap<>();

    public final Map<ResourceLocation, String> cachedRunicNames = new WeakHashMap<>();

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
            cachedRunicNames.put(id, Helpers.generateRunes(location, seed, 12));
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

    public int familiarity(RunestoneLocation location) {
        var id = location.id();
        if (knowledge == null) {
            return 0;
        }
        return knowledge.locations().getOrDefault(id, 0);
    }

    public MutableComponent nameWithFamiliarity(RunestoneBlockEntity runestone) {
        var minecraft = Minecraft.getInstance();
        var location = runestone.location;
        var pos = runestone.getBlockPos();
        var seed = pos.asLong();
        var familiarity = familiarity(location);

        if (familiarity == 0 || !feature().showNameFamiliarity()) {
            return Component.translatable("gui.charmony-runestones.runestone.unknown");
        }

        if (minecraft.level == null) {
            throw new RuntimeException("Should not be called when level is not loaded");
        }

        var gameTime = minecraft.level.getGameTime();
        if (nameCache.containsKey(pos) && gameTime < lastNameCache + 100) {
            return nameCache.get(pos);
        }

        feature().log().debug("Rebuilding name cache");
        var translated = Component.translatable(Helpers.localeKey(location)).getString();
        var rand = RandomSource.create(seed);
        var revealed = ((familiarity - 1) * 2) + 1; // This is the max number of letters that are revealed

        // Build a string of ?s that matches the length of the location's translated name.
        var out = String.valueOf(UNKNOWN_LETTER).repeat(translated.length());

        var passes = 0; // Restrict to a number of passes to avoid infinite loop
        while (revealed > 0 && passes < 4) {
            var outLetters = out.toCharArray();
            for (var i = outLetters.length - 1; i >= 0; i--) {
                rand.nextInt();
                var outLetter = out.charAt(i);
                var actualLetter = translated.charAt(i);

                // More chance to reveal inner letters, and the overall chance is increased by familiarity
                var chance = ((i == 0 || i == outLetters.length - 1) ? 0.03d : 0.2d) + ((familiarity - 1) * 0.1d);

                if (outLetter == UNKNOWN_LETTER && rand.nextDouble() < chance) {
                    outLetters[i] = actualLetter; // Replace the ? with the actual letter.
                    revealed--;
                }
                if (revealed <= 0) break;
            }
            out = String.copyValueOf(outLetters);
            passes++;
        }

        var name = Component.translatable("gui.charmony-runestones.runestone.familiar", out);
        nameCache.clear();
        nameCache.put(pos, name);
        lastNameCache = gameTime;
        return name;
    }
}
