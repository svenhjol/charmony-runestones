package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluids;
import svenhjol.charmony.api.StoneCircleDefinition;
import svenhjol.charmony.core.helpers.TagHelper;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import java.util.*;
import java.util.function.Supplier;

public class StoneCirclePiece extends ScatteredFeaturePiece {
    private static final String DEFINITION_TAG = "stone_circle_definition";
    private StoneCircleDefinition definition;

    public StoneCirclePiece(StoneCircleDefinition definition, BlockPos startPos, RandomSource random) {
        super(StoneCircles.feature().registers.structurePiece.get(), startPos.getX(), startPos.getY(), startPos.getZ(), 16, 8, 16,
            getRandomHorizontalDirection(random));
        this.definition = definition;
    }

    public StoneCirclePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StoneCircles.feature().registers.structurePiece.get(), tag);
        tag.getString(DEFINITION_TAG).ifPresent(t -> this.definition = StoneCircles.feature().registers.definitions.get(t));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString(DEFINITION_TAG, this.definition.getSerializedName());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {

        var name = definition.name();
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
        var decayChance = definition.decayChance();
        var runestoneBlock = definition.runestoneBlock().map(Supplier::get).orElse(null);
        var numberOfRunestonesAddedToCircle = 0;
        var runestoneQuality = definition.runestoneQuality();
        var maxRunestonesPerCircle = definition.maxRunestonesPerCircle();
        var maxRunestonesPerPillar = definition.maxRunestonesPerPillar();
        var runestoneChance = definition.runestoneChance();
        var generatedAnything = false;

        if (!StoneCircles.feature().enabled()) {
            return; // Don't generate any blocks if feature disabled.
        }

        if (!Runestones.feature().enabled()) {
            runestoneChance = 0; // Don't generate any runestones if runestones feature is disabled.
        }

        StoneCircles.feature().log().debug("Attempting to generate stone circle of type " + name + " at " + blockPos);

        // Avoid stone circles being placed on the roof of the dimension.
        // Delegate to the definition to reposition the stone circle.
        if (level.dimensionType().hasCeiling()) {
            blockPos = definition.ceilingReposition(level, blockPos);
        }

        Map<BlockPos, BlockState> decay = new HashMap<>();

        // Generate item in center of circle.
        for (int cy = maxHeightTolerance; cy > minHeightTolerance; cy--) {
            var centerPos = blockPos.offset(0, cy, 0);
            var centerUpPos = centerPos.above();
            var centerState = level.getBlockState(centerPos);
            var centerUpState = level.getBlockState(centerUpPos);

            var validCenterPos = centerState.canOcclude() && centerUpState.isAir();
            if (!validCenterPos) {
                continue;
            }

            definition.addAtCenter(level, centerUpPos);
            break;
        }

        // Generate pillars in a rough circle.
        for (int i = 0; i < 360; i += degrees + (circleJitter > 0 ? random.nextInt(circleJitter + 1) - circleJitter : 0)) {
            if (360 - i < minDegrees) continue;
            var x = (int)(radius * Math.cos(i * Math.PI / 180));
            var z = (int)(radius * Math.sin(i * Math.PI / 180));
            var numberOfRunestonesAddedToPillar = 0;

            for (int s = maxHeightTolerance; s > minHeightTolerance; s--) {
                var checkPos = blockPos.offset(x, s, z);
                var checkUpPos = checkPos.above();
                var checkState = level.getBlockState(checkPos);
                var checkUpState = level.getBlockState(checkUpPos);
                var canAddRunestone = runestoneBlock != null && (numberOfRunestonesAddedToCircle < maxRunestonesPerCircle);

                var validSurfacePos = ((checkState.canOcclude() || checkState.getFluidState().is(Fluids.LAVA))
                    && (checkUpState.isAir() || !checkUpState.canOcclude() || level.isWaterAt(checkUpPos)));
                if (!validSurfacePos) {
                    continue;
                }

                random.nextDouble();
                var pillarHeight = random.nextInt(maxPillarHeight - minPillarHeight) + minPillarHeight;

                for (int px = 0; px < pillarThickness; px++) {
                    for (int pz = 0; pz < pillarThickness; pz++) {
                        level.setBlock(checkPos.north(pz).east(px), getRandomBlock(pillarBlocks, random), 2);
                    }
                }

                // Generate debris around the pillar's base.
                for (int debrisRolls = 0; debrisRolls < definition.debrisRolls(); debrisRolls++) {
                    for (int dy = -minPillarHeight; dy <= 1; dy++) {
                        if (random.nextDouble() > definition.debrisChance()) continue;
                        var range = definition.debrisRange();

                        var dx = random.nextIntBetweenInclusive(-range, range) / 2;
                        var dz = random.nextIntBetweenInclusive(-range, range) / 2;

                        var gravelPos = checkPos.offset(dx, dy, dz);
                        var gravelUpPos = gravelPos.above();
                        var gravelState = level.getBlockState(gravelPos);
                        var gravelUpState = level.getBlockState(gravelUpPos);

                        var validGravelPos = ((gravelState.canOcclude() || gravelState.getFluidState().is(Fluids.LAVA))
                            && (gravelUpState.canOcclude() || !level.isWaterAt(gravelUpPos)));
                        if (!validGravelPos) {
                            continue;
                        }

                        if (random.nextDouble() < 0.8f) {
                            tryReplaceFloorBlock(level, gravelPos, random);
                        } else {
                            var state = getRandomBlock(pillarBlocks, random);
                            level.setBlock(gravelPos, state, 3);
                        }
                    }
                }

                // Generate the pillar.
                for (int y = -minPillarHeight; y < pillarHeight; y++) {
                    var pillarYPos = checkPos.above(y);
                    var isTop = y == pillarHeight - 1;
                    var hasSolidNeighbour = false;
                    var inverse = random.nextBoolean();

                    for (int px = 0; px < pillarThickness; px++) {
                        for (int pz = 0; pz < pillarThickness; pz++) {
                            var pillarYState = getRandomBlock(pillarBlocks, random);
                            var ppx = inverse ? pillarThickness - 1 - px : px;
                            var ppz = inverse ? pillarThickness - 1 - pz : pz;
                            var offset = pillarYPos.north(ppz).east(ppx);
                            var isEdgeOfPillar = (ppx == 0 || ppx == pillarThickness - 1) && (ppz == 0 || ppz == pillarThickness - 1);

                            if (canAddRunestone && isEdgeOfPillar
                                && random.nextDouble() < runestoneChance
                                && random.nextDouble() < 0.0d + (isTop ? 1.0d : (0.5d * ((double) y / pillarHeight)))) {
                                level.setBlock(offset, runestoneBlock.defaultBlockState(), 2);
                                Runestones.feature().handlers.prepare(level, offset, runestoneQuality);
                                ++numberOfRunestonesAddedToCircle;
                                ++numberOfRunestonesAddedToPillar;

                                canAddRunestone = numberOfRunestonesAddedToCircle < maxRunestonesPerCircle
                                    && numberOfRunestonesAddedToPillar < maxRunestonesPerPillar;
                            } else {
                                if (hasSolidNeighbour && random.nextDouble() < (decayChance * (isTop ? 1.0d : (0.65d * ((double) y / pillarHeight))))) {
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
        } else if (numberOfRunestonesAddedToCircle > 0) {
            StoneCircles.feature().log().debug("Generated " + numberOfRunestonesAddedToCircle + " runestones at " + blockPos);
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

    private void tryReplaceFloorBlock(WorldGenLevel level, BlockPos pos, RandomSource random) {
        var log = StoneCircles.feature().log();
        var replacements = definition.debrisReplacements();
        var state = level.getBlockState(pos);
        var original = state.getBlock();
        var seed = pos.asLong();

        if (replacements.containsKey(original)) {
            var pairs = new ArrayList<>(replacements.get(original));
            Collections.shuffle(pairs);

            for (var pair : pairs) {
                var block = pair.getFirst();
                var chance = pair.getSecond();

                if (random.nextDouble() > chance) {
                    continue;
                }

                level.setBlock(pos, block.defaultBlockState(), 3);

                switch (block) {
                    case BrushableBlock brushableBlock -> {
                        var lootTables = definition.archaeologyLootTables();

                        if (!lootTables.isEmpty()) {
                            var lootTable = lootTables.get(random.nextInt(lootTables.size()));
                            level.getBlockEntity(pos, BlockEntityType.BRUSHABLE_BLOCK)
                                .ifPresent(brushable -> brushable.setLootTable(lootTable, seed));
                        } else {
                            log.warn("Did not generate any loot for the brushable block at pos " + pos);
                        }
                    }
                    case ChestBlock chestBlock -> {
                        var lootTables = definition.chestLootTables();

                        if (!lootTables.isEmpty()) {
                            var lootTable = lootTables.get(random.nextInt(lootTables.size()));
                            level.getBlockEntity(pos, BlockEntityType.CHEST)
                                .ifPresent(chest -> chest.setLootTable(lootTable, seed));
                        } else {
                            log.warn("Did not generate any loot for the chest block at pos " + pos);
                        }
                    }
                    case BarrelBlock barrelBlock -> {
                        var lootTables = definition.barrelLootTables();

                        if (!lootTables.isEmpty()) {
                            var lootTable = lootTables.get(random.nextInt(lootTables.size()));
                            level.getBlockEntity(pos, BlockEntityType.BARREL)
                                .ifPresent(barrel -> barrel.setLootTable(lootTable, seed));
                        } else {
                            log.warn("Did not generate any loot for the barrel block at pos " + pos);
                        }
                    }
                    default -> {}
                }
                break;
            }
        }
    }
}
