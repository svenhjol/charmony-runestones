package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.util.Mth;
import svenhjol.charmony.core.annotations.Configurable;
import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.RunestonesMod;

@FeatureDefinition(side = Side.Common)
public final class Runestones extends SidedFeature {
    public final Registers registers;
    public final Handlers handlers;
    public final Providers providers;
    public final Advancements advancements;

    @Configurable(
        name = "Protection duration",
        description = "Duration (in seconds) of protection given to the player while they teleport via a runestone.",
        requireRestart = false
    )
    private static int protectionDuration = 3;

    public Runestones(Mod mod) {
        super(mod);
        registers = new Registers(this);
        handlers = new Handlers(this);
        providers = new Providers(this);
        advancements = new Advancements(this);
    }

    public static Runestones feature() {
        return RunestonesMod.instance().sidedFeature(Runestones.class);
    }

    public int protectionDuration() {
        return Mth.clamp(protectionDuration, 0, 60) * 20;
    }
}
