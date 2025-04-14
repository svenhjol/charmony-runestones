package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.util.Mth;
import svenhjol.charmony.core.annotations.Configurable;
import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;

@FeatureDefinition(side = Side.Common, description = """
    Adds small pillars of stone in all three dimensions.
    Runestones can often be found on the top of the stone pillars.""")
public final class StoneCircles extends SidedFeature {
    public static final String STRUCTURE_ID = "stone_circle";

    public final Handlers handlers;
    public final Registers registers;
    public final StoneCircleDefinitions stoneCircleDefinitions;
    public final RunestoneDefinitions runestoneDefinitions;

    @Configurable(
        name = "Stone circle runestone chance",
        description = """
            Chance (out of 1.0) of a runestone linking to another stone circle.
            This chance is further divided by other runestone destination calculations,
            so the actual chance is likely to be lower.""",
        requireRestart = false
    )
    private static double stoneCircleRunestoneChance = 0.3d;

    @Configurable(
        name = "Stone circle debris",
        description = """
            If true, stone circle pillars will generate with debris around their base.
            Debris can include suspicious sand and suspicious gravel, with loot tables
            selected from the stone circle definition. By default this selects from a range
            of overworld archaeology loot tables.""",
        requireRestart = false
    )
    private static boolean stoneCircleDebris = true;

    @Configurable(
        name = "Excavate runestones from debris",
        description = """
            If true, there is a chance to excavate a runestone when brushing suspicious gravel
            or suspicious sand from around the base of an overworld stone circle pillar.
            This allows a player to place down the runestone where they please, allowing convenient
            teleportation to the runestone's destination.
            This pairs well with the 'Harvestable runestones' option in the Runestones configuration.""",
        requireRestart = false
    )
    private static boolean excavateRunestonesFromDebris = false;

    public StoneCircles(Mod mod) {
        super(mod);

        handlers = new Handlers(this);
        registers = new Registers(this);
        stoneCircleDefinitions = new StoneCircleDefinitions(this);
        runestoneDefinitions = new RunestoneDefinitions(this);
    }

    public static StoneCircles feature() {
        return Mod.getSidedFeature(StoneCircles.class);
    }

    public double stoneCircleRunestoneChance() {
        return Mth.clamp(stoneCircleRunestoneChance, 0.0d, 1.0d);
    }

    public boolean stoneCircleDebris() {
        return stoneCircleDebris;
    }

    public boolean excavateRunestonesFromDebris() {
        return excavateRunestonesFromDebris;
    }
}
