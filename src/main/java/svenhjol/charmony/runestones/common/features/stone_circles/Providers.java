package svenhjol.charmony.runestones.common.features.stone_circles;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import svenhjol.charmony.api.*;
import svenhjol.charmony.core.Api;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneHelper;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class Providers extends Setup<StoneCircles> implements StoneCircleDefinitionsProvider, RunestoneDefinitionsProvider {
    public static final String DEFAULT = "stone";

    public Codec<StoneCircleDefinition> codec;
    public final Map<String, StoneCircleDefinition> definitions = new HashMap<>();

    public Providers(StoneCircles feature) {
        super(feature);

        Api.registerProvider(this);

        // This class is a consumer of StoneCircleDefinitions.
        Api.consume(StoneCircleDefinitionsProvider.class, provider -> {
            for (var definition : provider.getStoneCircleDefinitions()) {
                this.definitions.put(definition.name(), definition);
            }

            codec = StringRepresentable.fromValues(
                () -> definitions.values().toArray(new StoneCircleDefinition[0]));
        });
    }

    @Override
    public List<StoneCircleDefinition> getStoneCircleDefinitions() {
        return List.of(
            stoneCirclesForOverworld(),
            overgrownStoneCirclesForOverworld(),
            stoneCirclesForTheNether(),
            stoneCirclesForTheEnd());
    }

    @Override
    public List<RunestoneDefinition> getRunestoneDefinitions() {
        return List.of(
            stoneCircleRunestoneDefinition(),
            blackstoneCircleRunestoneDefinition(),
            obsidianCircleRunestoneDefinition()
        );
    }

    private StoneCircleDefinition stoneCirclesForOverworld() {
        return new StoneCircleDefinition() {
            @Override
            public String name() {
                return DEFAULT;
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return TagKey.create(Registries.BLOCK, RunestonesMod.id("stone_circle/stone_pillar_blocks"));
            }

            @Override
            public Pair<Integer, Integer> pillarHeight() {
                return Pair.of(3, 8);
            }

            @Override
            public Pair<Integer, Integer> pillarThickness() {
                return Pair.of(1, 2);
            }

            @Override
            public double decayChance() {
                return 0.1d;
            }

            @Override
            public Pair<Integer, Integer> radius() {
                return Pair.of(6, 14);
            }

            @Override
            public Pair<Integer, Integer> degrees() {
                return Pair.of(40, 50);
            }

            @Override
            public int circleJitter() {
                return 1;
            }

            @Override
            public Optional<Supplier<? extends Block>> runestoneBlock() {
                return Optional.ofNullable(Runestones.feature().registers.stoneBlock);
            }
        };
    }

    private StoneCircleDefinition overgrownStoneCirclesForOverworld() {
        return new StoneCircleDefinition() {
            @Override
            public String name() {
                return "overgrown_stone";
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return TagKey.create(Registries.BLOCK, RunestonesMod.id("stone_circle/overgrown_stone_pillar_blocks"));
            }

            @Override
            public Pair<Integer, Integer> pillarHeight() {
                return Pair.of(9, 16);
            }

            @Override
            public Pair<Integer, Integer> pillarThickness() {
                return Pair.of(2, 3);
            }

            @Override
            public double decayChance() {
                return 0.37d;
            }

            @Override
            public double runestoneChance() {
                return 0.28d;
            }

            @Override
            public double runestoneQuality() {
                return 0.08d;
            }

            @Override
            public int maxRunestonesPerPillar() {
                return 3;
            }

            @Override
            public int maxRunestonesPerCircle() {
                return 12;
            }

            @Override
            public Pair<Integer, Integer> radius() {
                return Pair.of(12, 18);
            }

            @Override
            public Pair<Integer, Integer> degrees() {
                return Pair.of(35, 55);
            }

            @Override
            public int circleJitter() {
                return 1;
            }

            @Override
            public Optional<Supplier<? extends Block>> runestoneBlock() {
                return Optional.ofNullable(Runestones.feature().registers.stoneBlock);
            }
        };
    }

    private StoneCircleDefinition stoneCirclesForTheNether() {
        return new StoneCircleDefinition() {
            @Override
            public String name() {
                return "blackstone";
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return TagKey.create(Registries.BLOCK, RunestonesMod.id("stone_circle/blackstone_pillar_blocks"));
            }

            @Override
            public Pair<Integer, Integer> pillarHeight() {
                return Pair.of(5, 7);
            }

            @Override
            public Pair<Integer, Integer> radius() {
                return Pair.of(10, 12);
            }

            @Override
            public Pair<Integer, Integer> pillarThickness() {
                return Pair.of(1, 3);
            }

            @Override
            public double decayChance() {
                return 0.24d;
            }

            @Override
            public Pair<Integer, Integer> degrees() {
                return Pair.of(30, 45);
            }

            @Override
            public int circleJitter() {
                return 2;
            }

            @Override
            public int maxRunestonesPerPillar() {
                return 3;
            }

            @Override
            public int maxRunestonesPerCircle() {
                return 10;
            }

            @Override
            public int terrainHeightTolerance() {
                return 40;
            }

            @Override
            public BlockPos ceilingReposition(WorldGenLevel level, BlockPos pos) {
                var foundSpace = false;
                var min = level.getMinY() + 15;
                var random = level.getRandom();
                var maxTries = 8;

                for (int tries = 1; tries <= maxTries; tries++) {
                    var x = pos.getX() + random.nextInt(tries * 2) - tries;
                    var z = pos.getZ() + random.nextInt(tries * 2) - tries;

                    for (int i = pos.getY() - 30; i > min; i--) {
                        var checkPos = new BlockPos(x, i, z);
                        var checkState = level.getBlockState(checkPos);
                        var checkBelowState = level.getBlockState(checkPos.below());

                        if (checkState.isAir() && (checkBelowState.canOcclude() || checkBelowState.getFluidState().is(Fluids.LAVA))) {
                            pos = checkPos;
                            foundSpace = true;
                            break;
                        }
                    }

                    if (foundSpace) break;
                }

                if (!foundSpace) {
                    pos = StoneCircleDefinition.super.ceilingReposition(level, pos);
                }
                return pos;
            }

            @Override
            public Optional<Supplier<? extends Block>> runestoneBlock() {
                return Optional.ofNullable(Runestones.feature().registers.blackstoneBlock);
            }
        };
    }

    private StoneCircleDefinition stoneCirclesForTheEnd() {
        return new StoneCircleDefinition() {
            @Override
            public String name() {
                return "obsidian";
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return TagKey.create(Registries.BLOCK, RunestonesMod.id("stone_circle/obsidian_pillar_blocks"));
            }

            @Override
            public Pair<Integer, Integer> pillarHeight() {
                return Pair.of(5, 9);
            }

            @Override
            public Pair<Integer, Integer> pillarThickness() {
                return Pair.of(1, 2);
            }

            @Override
            public double decayChance() {
                return 0.1d;
            }

            @Override
            public Pair<Integer, Integer> radius() {
                return Pair.of(10, 16);
            }

            @Override
            public Pair<Integer, Integer> degrees() {
                return Pair.of(35, 55);
            }

            @Override
            public double runestoneChance() {
                return 0.5d;
            }

            @Override
            public int maxRunestonesPerCircle() {
                return 10;
            }

            @Override
            public Optional<Supplier<? extends Block>> runestoneBlock() {
                return Optional.ofNullable(Runestones.feature().registers.obsidianBlock);
            }
        };
    }

    private RunestoneDefinition stoneCircleRunestoneDefinition() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return Runestones.feature().registers.stoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRACKED_STONE_BRICKS;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                if (feature().enabled() && random.nextDouble() < feature().stoneCircleRunestoneChance()) {
                    return Optional.of(new RunestoneLocation(RunestoneLocation.Type.Structure, RunestonesMod.id("stone_circle_stone")));
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> RunestoneHelper.randomItem(level, random, "runestone/stone/common_items");
            }
        };
    }

    private RunestoneDefinition blackstoneCircleRunestoneDefinition() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return Runestones.feature().registers.blackstoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                if (feature().enabled() && random.nextDouble() < feature().stoneCircleRunestoneChance()) {
                    return Optional.of(new RunestoneLocation(RunestoneLocation.Type.Structure, RunestonesMod.id("stone_circle_blackstone")));
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> RunestoneHelper.randomItem(level, random, "runestone/blackstone/common_items");
            }
        };
    }

    private RunestoneDefinition obsidianCircleRunestoneDefinition() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return Runestones.feature().registers.obsidianBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRYING_OBSIDIAN;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                if (feature().enabled() && random.nextDouble() < feature().stoneCircleRunestoneChance()) {
                    return Optional.of(new RunestoneLocation(RunestoneLocation.Type.Structure, RunestonesMod.id("stone_circle_obsidian")));
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> RunestoneHelper.randomItem(level, random, "runestone/obsidian/common_items");
            }
        };
    }
}
