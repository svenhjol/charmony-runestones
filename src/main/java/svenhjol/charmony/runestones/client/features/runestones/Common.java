package svenhjol.charmony.runestones.client.features.runestones;

import svenhjol.charmony.runestones.common.features.runestones.Registers;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

public final class Common {
    public final Registers registers;

    public Common() {
        var common = Runestones.feature();
        registers = common.registers;
    }
}
