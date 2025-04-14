package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.util.Mth;
import svenhjol.charmony.core.annotations.Configurable;
import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;

@FeatureDefinition(side = Side.Common, canBeDisabled = false, description = """
    Runestones are blocks that teleport nearby players to interesting structures and biomes.
    Each runestone shows what item is required to teleport.
    Drop the item near the runestone to activate it.""")
public final class Runestones extends SidedFeature {
    public final Registers registers;
    public final Handlers handlers;
    public final Advancements advancements;
    public final RunestoneDefinitions runestoneDefinitions;
    public final RuneWords runeWords;

    @Configurable(
        name = "Harvestable runestones",
        description = """
            If true, runestones can be mined with a pickaxe and be picked up by the player.
            If false, runestones will break when mined with any tool.""",
        requireRestart = false
    )
    private static boolean harvestable = false;

    @Configurable(
        name = "Familiarity",
        description = """
            If true, the player remembers the location type when travelling via a runestone.
            Future runestones of the same location type will be revealed when looking at a runestone.""",
        requireRestart = false
    )
    private static boolean familiarity = true;

    @Configurable(
        name = "Protection duration",
        description = "Duration (in seconds) of protection given to the player while they teleport via a runestone.",
        requireRestart = false
    )
    private static int protectionDuration = 3;

    @Configurable(
        name = "Link to stronghold",
        description = """
            If true, there is a rare chance for a runestone to link to a stronghold.
            Runestones that spawn in stone circles have a 1% chance to link to a stronghold.
            Runestones that spawn in rare structures have a greater chance to link to a stronghold.""",
        requireRestart = false
    )
    private static boolean linkToStronghold = true;

    public Runestones(Mod mod) {
        super(mod);
        handlers = new Handlers(this);
        registers = new Registers(this);
        advancements = new Advancements(this);
        runestoneDefinitions = new RunestoneDefinitions(this);
        runeWords = new RuneWords(this);
    }

    public static Runestones feature() {
        return Mod.getSidedFeature(Runestones.class);
    }

    public boolean harvestable() {
        return harvestable;
    }

    public int protectionDuration() {
        return Mth.clamp(protectionDuration, 0, 60) * 20;
    }

    public boolean linkToStronghold() {
        return linkToStronghold;
    }

    public boolean familiarity() {
        return familiarity;
    }
}
