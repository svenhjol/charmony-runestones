package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import svenhjol.charmony.core.common.features.teleport.Teleporter;
import svenhjol.charmony.rune_dictionary.common.features.rune_dictionary.RuneDictionary;

public class RunestoneTeleporter extends Teleporter {
    private final RunestoneBlockEntity runestone;
    private ResourceKey<Level> dimension;
    private Vec3 target;

    public RunestoneTeleporter(ServerPlayer player, RunestoneBlockEntity runestone) {
        super(player);

        this.runestone = runestone;

        if (Helpers.runestoneLinksToSpawnPoint(runestone)) {
            // Handle world spawn point runestone.
            setSpawnPoint();
        } else {
            // All standard runestones just use the same dimension and fixed target pos.
            this.dimension = getLevel().dimension();

            if (runestone.target().isEmpty()) {
                LOGGER.error("runestone.target is not set: " + runestone);
                setSpawnPoint();
            } else {
                this.target = runestone.target().get().getCenter();
            }
        }
    }

    @Override
    protected ResourceKey<Level> getTargetDimension() {
        return dimension;
    }

    @Override
    protected Vec3 getTargetPos() {
        return target;
    }

    @Override
    protected int protectionDurationTicks() {
        return feature().protectionDuration();
    }

    @Override
    protected void doEffects() {
        // Play dimensional travel sound.
        super.doEffects();

        // Do advancements.
        var player = getPlayer();

        feature().advancements.travelledViaRunestone(player);
        if (Helpers.runestoneLinksToSpawnPoint(runestone)) {
            feature().advancements.travelledHomeViaRunestone(player);
        }

        var countWords = RuneDictionary.feature().handlers.knownWords(player).size();
        if (countWords >= 5) {
            feature().advancements.learnedFiveWords(player);
        }
        if (countWords >= 15) {
            feature().advancements.learnedFifteenWords(player);
        }
        if (countWords >= 30) {
            feature().advancements.learnedThirtyWords(player);
        }

        // Tell the client the location of where the player travelled to.
        Networking.S2CTeleportedLocation.send(player, runestone.location);
    }

    @Override
    protected void playTeleportSound() {
        getLevel().playSound(null,
            getPlayer().blockPosition(),
            feature().registers.travelSound.get(),
            SoundSource.BLOCKS);
    }

    private Runestones feature() {
        return Runestones.feature();
    }

    private void setSpawnPoint() {
        this.dimension = Level.OVERWORLD;
        this.target = getLevel().getSharedSpawnPos().getCenter();
    }
}
