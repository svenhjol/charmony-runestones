package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.helpers.AdvancementHelper;

public class Advancements extends Setup<Runestones> {
    public Advancements(Runestones feature) {
        super(feature);
    }
    
    public void lookedAtRunestone(Player player) {
        AdvancementHelper.trigger("looked_at_runestone", player);
    }
    
    public void travelledViaRunestone(Player player) {
        AdvancementHelper.trigger("travelled_via_runestone", player);
    }
    
    public void travelledHomeViaRunestone(Player player) {
        AdvancementHelper.trigger("travelled_home_via_runestone", player);
    }

    public void learnedFiveWords(Player player) {
        AdvancementHelper.trigger("learned_five_words", player);
    }

    public void learnedFifteenWords(Player player) {
        AdvancementHelper.trigger("learned_fifteen_words", player);
    }

    public void learnedThirtyWords(Player player) {
        AdvancementHelper.trigger("learned_thirty_words", player);
    }
}
