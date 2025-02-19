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

    public final Registers registers;
    public final Providers providers;

    @Configurable(
        name = "Stone circle runestone chance",
        description = """
            Chance (out of 1.0) of a runestone linking to another stone circle.
            This chance is further divided by other runestone destination calculations,
            so the actual chance is likely to be lower."""
    )
    private static double stoneCircleRunestoneChance = 0.3d;

    public StoneCircles(Mod mod) {
        super(mod);

        registers = new Registers(this);
        providers = new Providers(this);
    }

    public static StoneCircles feature() {
        return Mod.getSidedFeature(StoneCircles.class);
    }

    public double stoneCircleRunestoneChance() {
        return Mth.clamp(stoneCircleRunestoneChance, 0.0d, 1.0d);
    }
}
