package svenhjol.charmony.runestones.integration;

import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.integration.BaseModMenuPlugin;
import svenhjol.charmony.runestones.RunestonesMod;

public class ModMenuPlugin extends BaseModMenuPlugin {
    @Override
    public Mod mod() {
        return RunestonesMod.instance();
    }
}
