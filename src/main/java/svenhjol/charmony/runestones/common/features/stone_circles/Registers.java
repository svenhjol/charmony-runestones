package svenhjol.charmony.runestones.common.features.stone_circles;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import svenhjol.charmony.api.StoneCircleDefinition;
import svenhjol.charmony.api.StoneCircleDefinitionProvider;
import svenhjol.charmony.core.Api;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.common.CommonRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Registers extends Setup<StoneCircles> {
    private static final String PIECE_ID = "stone_circle_piece";

    public final Supplier<StructureType<StoneCircleStructure>> structureType;
    public final Supplier<StructurePieceType> structurePiece;
    public Codec<StoneCircleDefinition> codec;
    public final Map<String, StoneCircleDefinition> definitions = new HashMap<>();

    public Registers(StoneCircles feature) {
        super(feature);
        var registry = CommonRegistry.forFeature(feature);

        structureType = registry.structure(StoneCircles.STRUCTURE_ID, () -> StoneCircleStructure.CODEC);
        structurePiece = registry.structurePiece(PIECE_ID, () -> StoneCirclePiece::new);

        // Consumer of StoneCircleDefinitions.
        Api.consume(StoneCircleDefinitionProvider.class, provider -> {
            for (var definition : provider.getStoneCircleDefinitions()) {
                this.definitions.put(definition.name(), definition);
            }

            codec = StringRepresentable.fromValues(
                () -> definitions.values().toArray(new StoneCircleDefinition[0]));
        });
    }

    @Override
    public Runnable boot() {
        return () -> {
            var registry = CommonRegistry.forFeature(feature());

            registry.villagerTrade(() -> VillagerProfession.CARTOGRAPHER,
                2,
                () -> new VillagerTrades.TreasureMapForEmeralds(
                    7, // emerald cost
                    Tags.ON_STONE_CIRCLE_MAPS,
                    "filled_map.charmony-runestones.stone_circle",
                    MapDecorationTypes.TARGET_X,
                    12, // max trades
                    5) // XP for villager
            );
        };
    }
}
