package svenhjol.charmony.runestones;

import svenhjol.charmony.api.core.ModDefinition;
import svenhjol.charmony.api.core.Side;
import svenhjol.charmony.core.base.Mod;

@ModDefinition(
    id = RunestonesMod.ID,
    sides = {Side.Client, Side.Common},
    name = "Runestones",
    description = "Adds runestones and stone circles that allow players to teleport to interesting places."
)
public final class RunestonesMod extends Mod {
    public static final String ID = "charmony-runestones";
    private static RunestonesMod instance;

    private RunestonesMod() {}

    public static RunestonesMod instance() {
        if (instance == null) {
            instance = new RunestonesMod();
        }
        return instance;
    }
}
