package svenhjol.charmony.runestones.common.features.stone_circles;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import svenhjol.charmony.api.Api;
import svenhjol.charmony.api.runestones.RunestoneDefinition;
import svenhjol.charmony.api.runestones.RunestoneDefinitionProvider;
import svenhjol.charmony.api.runestones.RunestoneLocation;
import svenhjol.charmony.api.runestones.RunestoneType;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.common.features.runestones.Helpers;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RunestoneDefinitions extends Setup<StoneCircles> implements RunestoneDefinitionProvider {
    public RunestoneDefinitions(StoneCircles feature) {
        super(feature);
        Api.registerProvider(this);
    }

    @Override
    public List<RunestoneDefinition> getRunestoneDefinitions() {
        return List.of(
            stoneCircleRunestoneDefinition(),
            blackstoneCircleRunestoneDefinition(),
            obsidianCircleRunestoneDefinition()
        );
    }

    private RunestoneDefinition stoneCircleRunestoneDefinition() {
        return new RunestoneDefinition() {
            @Override
            public RunestoneType type() {
                return RunestoneType.Stone;
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
                return () -> Helpers.randomItem(level, random, "runestone/stone/common_items");
            }
        };
    }

    private RunestoneDefinition blackstoneCircleRunestoneDefinition() {
        return new RunestoneDefinition() {
            @Override
            public RunestoneType type() {
                return RunestoneType.Blackstone;
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
                return () -> Helpers.randomItem(level, random, "runestone/blackstone/common_items");
            }
        };
    }

    private RunestoneDefinition obsidianCircleRunestoneDefinition() {
        return new RunestoneDefinition() {
            @Override
            public RunestoneType type() {
                return RunestoneType.Obsidian;
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
                return () -> Helpers.randomItem(level, random, "runestone/obsidian/common_items");
            }
        };
    }
}
