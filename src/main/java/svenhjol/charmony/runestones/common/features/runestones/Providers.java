package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import svenhjol.charmony.api.RunestoneDefinition;
import svenhjol.charmony.api.RunestoneDefinitionsProvider;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.Api;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.common.CommonRegistry;
import svenhjol.charmony.runestones.RunestonesMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class Providers extends Setup<Runestones> implements RunestoneDefinitionsProvider {
    public final List<RunestoneDefinition> definitions = new ArrayList<>();

    public Providers(Runestones feature) {
        super(feature);
    }

    @Override
    public Runnable boot() {
        return () -> {
            var registry = CommonRegistry.forFeature(feature());

            // This class is a provider of runestone definitions.
            Api.registerProvider(this);

            // This class is also a consumer of definitions.
            Api.consume(RunestoneDefinitionsProvider.class, providers -> {
                for (var definition : providers.getRunestoneDefinitions()) {
                    // Add the block to the runestone block entity.
                    var blockSupplier = definition.runestoneBlock();
                    registry.blocksForBlockEntity(feature().registers.blockEntity, List.of(blockSupplier));

                    // Add the definition to the full set for mapping later.
                    definitions.add(definition);
                }
            });
        };
    }

    @Override
    public List<RunestoneDefinition> getRunestoneDefinitions() {
        return List.of(
            stoneCommon(),
            stoneUncommon(),
            stoneRare(),
            stoneSpawnPoint(),
            blackstone(),
            blackstoneSpawnPoint(),
            obsidian(),
            obsidianSpawnPoint()
        );
    }

    private RunestoneDefinition stoneCommon() {
        return new CustomRunestoneDefinition(0.5d, 0.9d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.stoneBlock;
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
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/common_items");
            }
        };
    }

    private RunestoneDefinition stoneUncommon() {
        return new CustomRunestoneDefinition(0.32d, 0.55d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.stoneBlock;
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
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/uncommon_items");
            }
        };
    }

    private RunestoneDefinition stoneRare() {
        return new CustomRunestoneDefinition(0.2d, 0.33d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.stoneBlock;
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
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/rare_items");
            }
        };
    }

    private RunestoneDefinition stoneSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.stoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.MOSSY_COBBLESTONE;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                if (random.nextDouble() < 0.33d) {
                    return Optional.of(Helpers.SPAWN_POINT);
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/stone/spawn_point_items");
            }
        };
    }

    private RunestoneDefinition blackstone() {
        return new CustomRunestoneDefinition(0.6d, 0.9d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.blackstoneBlock;
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
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/blackstone/common_items");
            }
        };
    }

    private RunestoneDefinition blackstoneSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.blackstoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.GILDED_BLACKSTONE;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                if (random.nextDouble() < 0.33d) {
                    return Optional.of(Helpers.SPAWN_POINT);
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/blackstone/spawn_point_items");
            }
        };
    }

    private RunestoneDefinition obsidian() {
        return new CustomRunestoneDefinition(0.5d, 0.9d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.obsidianBlock;
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
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/obsidian/common_items");
            }
        };
    }

    private RunestoneDefinition obsidianSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.obsidianBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRYING_OBSIDIAN;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                if (random.nextDouble() < 0.25d) {
                    return Optional.of(Helpers.SPAWN_POINT);
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/obsidian/spawn_point_items");
            }
        };
    }

    /**
     * A simple implementation of RunestoneDefinition with chance of a runestone to link to a biome or a structure.
     */
    private abstract static class CustomRunestoneDefinition implements RunestoneDefinition {
        private final double biomeChance;
        private final double structureChance;

        public CustomRunestoneDefinition(double biomeChance, double structureChance) {
            this.biomeChance = biomeChance;
            this.structureChance = structureChance;
        }

        @Override
        public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
            if (random.nextDouble() < biomeChance) {
                return Helpers.randomBiome(level, random, TagKey.create(Registries.BIOME, RunestonesMod.id(biomeTagPath())));
            }
            if (random.nextDouble() < structureChance) {
                return Helpers.randomStructure(level, random, TagKey.create(Registries.STRUCTURE, RunestonesMod.id(structureTagPath())));
            }

            return Optional.empty();
        }

        public abstract String biomeTagPath();

        public abstract String structureTagPath();
    }
}
