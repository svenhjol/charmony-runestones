package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.api.Api;
import svenhjol.charmony.api.RuneWordProvider;
import svenhjol.charmony.core.base.Setup;

import java.util.List;

public class RuneWords extends Setup<Runestones> implements RuneWordProvider {
    public RuneWords(Runestones feature) {
        super(feature);
        Api.registerProvider(this);
    }

    @Override
    public List<ResourceLocation> getRuneWords(RegistryAccess registryAccess) {
        // Add custom locations to the rune dictionary.
        return List.of(Helpers.SPAWN_POINT_ID);
    }
}
