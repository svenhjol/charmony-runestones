package svenhjol.charmony.runestones;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.core.annotations.ModDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.enums.Side;

@ModDefinition(
    id = RunestonesMod.ID,
    sides = {Side.Client, Side.Common},
    name = "Runestones"
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

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}
