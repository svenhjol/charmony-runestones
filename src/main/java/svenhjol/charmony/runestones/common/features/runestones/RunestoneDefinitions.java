package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import svenhjol.charmony.api.runestones.RunestoneDefinition;
import svenhjol.charmony.api.runestones.RunestoneDefinitionProvider;
import svenhjol.charmony.api.runestones.RunestoneLocation;
import svenhjol.charmony.api.runestones.RunestoneType;
import svenhjol.charmony.core.Api;
import svenhjol.charmony.core.Charmony;
import svenhjol.charmony.core.base.Setup;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RunestoneDefinitions extends Setup<Runestones> implements RunestoneDefinitionProvider {
    public RunestoneDefinitions(Runestones feature) {
        super(feature);
        Api.registerProvider(this);
    }

    @Override
    public List<RunestoneDefinition> getRunestoneDefinitions() {
        return List.of(
            stoneCommon(),
            stoneUncommon(),
            stoneRare(),
            stoneSpawnPoint(),
            stoneStronghold(),
            blackstone(),
            blackstoneSpawnPoint(),
            obsidian(),
            obsidianSpawnPoint()
        );
    }

    private RunestoneDefinition stoneCommon() {
        return new CustomRunestoneDefinition(0.5d, 0.9d, false) {
            @Override
            public RunestoneType type() {
                return RunestoneType.Stone;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.STONE;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/stone/common_biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/stone/common_structure_located";
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/common");
            }
        };
    }

    private RunestoneDefinition stoneUncommon() {
        return new CustomRunestoneDefinition(0.32d, 0.55d, false) {
            @Override
            public RunestoneType type() {
                return RunestoneType.Stone;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.MOSSY_COBBLESTONE;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/stone/uncommon_biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/stone/uncommon_structure_located";
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/uncommon");
            }
        };
    }

    private RunestoneDefinition stoneRare() {
        return new CustomRunestoneDefinition(0.2d, 0.33d, true) {
            @Override
            public RunestoneType type() {
                return RunestoneType.Stone;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.COBBLESTONE;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/stone/rare_biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/stone/rare_structure_located";
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/rare");
            }
        };
    }

    private RunestoneDefinition stoneSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public RunestoneType type() {
                return RunestoneType.Stone;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.MOSSY_COBBLESTONE;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                if (random.nextDouble() < 0.33d) {
                    return Optional.of(Helpers.SPAWN_POINT);
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/spawn_point");
            }
        };
    }

    private RunestoneDefinition stoneStronghold() {
        return new RunestoneDefinition() {
            @Override
            public RunestoneType type() {
                return RunestoneType.Stone;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.STONE_BRICKS;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                if (feature().linkToStronghold() && random.nextDouble() < (0.01d + quality)) {
                    return Optional.of(new RunestoneLocation(RunestoneLocation.Type.Structure, BuiltinStructures.STRONGHOLD.location()));
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/rare");
            }
        };
    }

    private RunestoneDefinition blackstone() {
        return new CustomRunestoneDefinition(0.45d, 0.9d, false) {
            @Override
            public RunestoneType type() {
                return RunestoneType.Blackstone;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.BLACKSTONE;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/blackstone/biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/blackstone/structure_located";
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/blackstone/common");
            }
        };
    }

    private RunestoneDefinition blackstoneSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public RunestoneType type() {
                return RunestoneType.Blackstone;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.GILDED_BLACKSTONE;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                if (random.nextDouble() < 0.33d) {
                    return Optional.of(Helpers.SPAWN_POINT);
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/blackstone/spawn_point");
            }
        };
    }

    private RunestoneDefinition obsidian() {
        return new CustomRunestoneDefinition(0.5d, 0.9d, false) {
            @Override
            public RunestoneType type() {
                return RunestoneType.Obsidian;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.OBSIDIAN;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/obsidian/biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/obsidian/structure_located";
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/obsidian/common");
            }
        };
    }

    private RunestoneDefinition obsidianSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public RunestoneType type() {
                return RunestoneType.Obsidian;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.OBSIDIAN;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                if (random.nextDouble() < 0.25d) {
                    return Optional.of(Helpers.SPAWN_POINT);
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> item(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
                return () -> Helpers.randomItem(level, random, "runestone/obsidian/spawn_point");
            }
        };
    }

    /**
     * A simple implementation of RunestoneDefinition with chance of a runestone to link to a biome or a structure.
     */
    private abstract static class CustomRunestoneDefinition implements RunestoneDefinition {
        private final double biomeChance; // Chance of this runestone linking to a biome.
        private final double structureChance; // Chance of this runestone linking to a structure.
        private final boolean isRare; // True if the runestone links to rare biomes and structures.

        public CustomRunestoneDefinition(double biomeChance, double structureChance, boolean isRare) {
            this.biomeChance = biomeChance;
            this.structureChance = structureChance;
            this.isRare = isRare;
        }

        @Override
        public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random, double quality) {
            var qualityChance = isRare ? quality : 0;
            if (random.nextDouble() < (biomeChance + qualityChance)) {
                return Helpers.randomBiome(level, random, TagKey.create(Registries.BIOME, Charmony.id(biomeTagPath())));
            }
            if (random.nextDouble() < (structureChance + qualityChance)) {
                return Helpers.randomStructure(level, random, TagKey.create(Registries.STRUCTURE, Charmony.id(structureTagPath())));
            }

            return Optional.empty();
        }

        public abstract String biomeTagPath();

        public abstract String structureTagPath();
    }
}
