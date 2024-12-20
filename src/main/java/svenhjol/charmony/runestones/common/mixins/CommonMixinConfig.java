package svenhjol.charmony.runestones.common.mixins;

import svenhjol.charmony.core.base.MixinConfig;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.RunestonesMod;

public class CommonMixinConfig extends MixinConfig {
    @Override
    protected String modId() {
        return RunestonesMod.ID;
    }

    @Override
    protected String modRoot() {
        return "svenhjol.charmony.runestones";
    }

    @Override
    protected Side side() {
        return Side.Common;
    }
}
