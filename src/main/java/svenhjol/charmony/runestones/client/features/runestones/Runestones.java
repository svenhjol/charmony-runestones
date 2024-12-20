package svenhjol.charmony.runestones.client.features.runestones;

import svenhjol.charmony.core.annotations.Configurable;
import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.RunestonesMod;

import java.util.function.Supplier;

@FeatureDefinition(side = Side.Client, canBeDisabled = false)
public final class Runestones extends SidedFeature {
    public final Supplier<Common> common;
    public final Registers registers;
    public final Handlers handlers;

    @Configurable(
        name = "Dim rune display background",
        description = "If true, focusing on a runestone will dim the background to make the text clearer.",
        requireRestart = false
    )
    private static boolean hudHasBackground = false;

    @Configurable(
        name = "Text shadow on rune display",
        description = "If true, adds a text shadow when focusing on a runestone.",
        requireRestart = false
    )
    private static boolean hudHasShadowText = true;

    public Runestones(Mod mod) {
        super(mod);
        common = Common::new;
        handlers = new Handlers(this);
        registers = new Registers(this);
    }

    public static Runestones feature() {
        return RunestonesMod.instance().sidedFeature(Runestones.class);
    }

    public boolean hudHasBackground() {
        return hudHasBackground;
    }

    public boolean hudHasShadowText() {
        return hudHasShadowText;
    }
}
