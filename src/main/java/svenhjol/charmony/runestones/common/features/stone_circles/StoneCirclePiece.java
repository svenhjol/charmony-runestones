package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluids;
import svenhjol.charmony.api.StoneCircleDefinition;
import svenhjol.charmony.core.helper.TagHelper;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StoneCirclePiece extends ScatteredFeaturePiece {
    private static final String DEFINITION_TAG = "stone_circle_definition";
    private final StoneCircleDefinition definition;

    public StoneCirclePiece(StoneCircleDefinition definition, BlockPos startPos, RandomSource random) {
        super(StoneCircles.feature().registers.structurePiece.get(), startPos.getX(), startPos.getY(), startPos.getZ(), 16, 8, 16,
            getRandomHorizontalDirection(random));
        this.definition = definition;
    }

    public StoneCirclePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StoneCircles.feature().registers.structurePiece.get(), tag);
        this.definition = StoneCircles.feature().providers.definitions.get(tag.getString(DEFINITION_TAG));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString(DEFINITION_TAG, this.definition.getSerializedName());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {

        var terrainHeightTolerance = definition.terrainHeightTolerance();
        var maxHeightTolerance = terrainHeightTolerance / 2; // If the surface Y value is this many blocks higher than starting Y, don't generate
        var minHeightTolerance = -(terrainHeightTolerance / 2); // If the surface Y value is this many blocks lower than starting Y, don't generate
        var minRadius = definition.radius().getFirst();
        var maxRadius = definition.radius().getSecond();
        var radius = random.nextInt(maxRadius - minRadius) + minRadius;
        var minPillarHeight = definition.pillarHeight().getFirst();
        var maxPillarHeight = definition.pillarHeight().getSecond();
        var pillarThickness = random.nextIntBetweenInclusive(definition.pillarThickness().getFirst(), definition.pillarThickness().getSecond());
        var pillarBlocks = getPillarBlocks(level);
        var minDegrees = definition.degrees().getFirst();
        var maxDegrees = definition.degrees().getSecond();
        var degrees = minDegrees + (random.nextInt((maxDegrees - minDegrees) + 1));
        var circleJitter = definition.circleJitter();
        var runestoneBlock = definition.runestoneBlock().map(Supplier::get).orElse(null);
        var generatedRunestones = 0;
        var runestoneQuality = definition.runestoneQuality();
        var maxRunestones = definition.maxRunestones();
        var runestoneChance = definition.runestoneChance();
        var generatedAnything = false;

        if (!StoneCircles.feature().enabled()) {
            return; // Don't generate any blocks if feature disabled.
        }

        if (!Runestones.feature().enabled()) {
            runestoneChance = 0; // Don't generate any runestones if runestones feature is disabled.
        }

        // Avoid stone circles being placed on the roof of the dimension.
        // Delegate to the definition to reposition the stone circle.
        if (level.dimensionType().hasCeiling()) {
            blockPos = definition.ceilingReposition(level, blockPos);
        }

        Map<BlockPos, BlockState> decay = new HashMap<>();

        // Generate pillars in a rough circle.
        for (int i = 0; i < 360; i += degrees + (circleJitter > 0 ? random.nextInt(circleJitter + 1) - circleJitter : 0)) {
            if (360 - i < minDegrees) continue;
            var x = (int)(radius * Math.cos(i * Math.PI / 180));
            var z = (int)(radius * Math.sin(i * Math.PI / 180));

            for (int s = maxHeightTolerance; s > minHeightTolerance; s--) {
                var checkPos = blockPos.offset(x, s, z);
                var checkUpPos = checkPos.above();
                var checkState = level.getBlockState(checkPos);
                var checkUpState = level.getBlockState(checkUpPos);
                var canGenerateRunestone = generatedRunestones < maxRunestones && runestoneBlock != null;

                var validSurfacePos = ((checkState.canOcclude() || checkState.getFluidState().is(Fluids.LAVA))
                    && (checkUpState.isAir() || !checkUpState.canOcclude() || level.isWaterAt(checkUpPos)));
                if (!validSurfacePos) {
                    continue;
                }

                var pillarHeight = random.nextInt(maxPillarHeight - minPillarHeight) + minPillarHeight;

                for (int px = 0; px < pillarThickness; px++) {
                    for (int pz = 0; pz < pillarThickness; pz++) {
                        level.setBlock(checkPos.north(pz).east(px), getRandomBlock(pillarBlocks, random), 2);
                    }
                }

                for (int y = 1; y < pillarHeight; y++) {
                    var pillarYPos = checkPos.above(y);
                    var isTop = y == pillarHeight - 1;
                    var hasSolidNeighbour = false;
                    var hasGeneratedRunestone = false;
                    var inverse = random.nextBoolean();

                    for (int px = 0; px < pillarThickness; px++) {
                        for (int pz = 0; pz < pillarThickness; pz++) {
                            var pillarYState = getRandomBlock(pillarBlocks, random);
                            var ppx = inverse ? pillarThickness - 1 - px : px;
                            var ppz = inverse ? pillarThickness - 1 - pz : pz;
                            var offset = pillarYPos.north(ppz).east(ppx);
                            var isEdgeOfPillar = (ppx == 0 || ppx == pillarThickness - 1) && (ppz == 0 || ppz == pillarThickness - 1);

                            if (canGenerateRunestone && !hasGeneratedRunestone && isEdgeOfPillar
                                && random.nextDouble() < runestoneChance
                                && random.nextDouble() < 0.0d + (isTop ? 1.0d : (0.5d * ((double) y / pillarHeight)))) {
                                level.setBlock(offset, runestoneBlock.defaultBlockState(), 2);
                                Runestones.feature().handlers.prepare(level, offset, runestoneQuality);
                                canGenerateRunestone = false;
                                hasGeneratedRunestone = true;
                                ++generatedRunestones;
                            } else {
                                if (hasSolidNeighbour && random.nextDouble() < (0.4d * (isTop ? 1.0d : 0.05d))) {
                                    decay.put(offset, level.getBlockState(offset));
                                }
                                level.setBlock(offset, pillarYState, 2);
                            }

                            hasSolidNeighbour = true;
                        }
                    }
                }

                // Apply decay.
                for (var entry : decay.entrySet()) {
                    level.setBlock(entry.getKey(), entry.getValue(), 2);
                }

                decay.clear();
                generatedAnything = true;
            }
        }

        if (!generatedAnything) {
            StoneCircles.feature().log().warn("Did not generate a stone circle at " + blockPos);
        } else if (generatedRunestones > 0) {
            StoneCircles.feature().log().debug("Generated " + generatedRunestones + " runestones at " + blockPos);
        }
    }

    private List<Block> getPillarBlocks(WorldGenLevel level) {
        var blocks = TagHelper.getValues(level.registryAccess().lookupOrThrow(Registries.BLOCK), definition.pillarBlocks());

        if (blocks.isEmpty()) {
            throw new RuntimeException("No pillar blocks to generate stone circle");
        }

        return blocks;
    }

    private BlockState getRandomBlock(List<Block> blocks, RandomSource random) {
        return blocks.get(random.nextInt(blocks.size())).defaultBlockState();
    }
}
