package svenhjol.charmony.runestones.common.features.stone_circles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import svenhjol.charmony.api.stone_circles.StoneCircleDefinition;

import java.util.Optional;

public class StoneCircleStructure extends Structure {
    public static final MapCodec<StoneCircleStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        StoneCircleStructure.settingsCodec(instance),
        StoneCircles.feature().registers.codec.fieldOf("stone_circle_definition")
            .forGetter(structure -> structure.definition))
            .apply(instance, StoneCircleStructure::new));

    private final StoneCircleDefinition definition;

    protected StoneCircleStructure(StructureSettings settings, StoneCircleDefinition definition) {
        super(settings);

        var defaultDefinition = StoneCircles.feature().registers.definitions.get(StoneCircleDefinitions.DEFAULT);
        if (defaultDefinition == null) {
            throw new RuntimeException("Missing default stone circle definition");
        }

        this.definition = StoneCircles.feature().registers.definitions.getOrDefault(definition.getSerializedName(), defaultDefinition);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        var opt = findStart(context);
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        var startPos = opt.get();
        return Optional.of(new GenerationStub(startPos,
            builder -> builder.addPiece(new StoneCirclePiece(definition, startPos, context.random()))));
    }

    private Optional<BlockPos> findStart(GenerationContext context) {
        var x = context.chunkPos().getMinBlockX();
        var z = context.chunkPos().getMinBlockZ();
        var min = context.heightAccessor().getMinY() + 15;
        var y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        var column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        var heightMap = Heightmap.Types.WORLD_SURFACE_WG;

        int surface;
        for (surface = y; surface > min; --surface) {
            var state = column.getBlock(y);
            var above = column.getBlock(y + 1);
            if (heightMap.isOpaque().test(state) && (!heightMap.isOpaque().test(above))) {
                return Optional.of(new BlockPos(x, surface, z));
            }
        }

        return Optional.empty();
    }

    @Override
    public StructureType<?> type() {
        return StoneCircles.feature().registers.structureType.get();
    }
}
