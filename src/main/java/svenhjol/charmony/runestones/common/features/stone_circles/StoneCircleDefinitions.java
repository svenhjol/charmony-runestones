package svenhjol.charmony.runestones.common.features.stone_circles;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import svenhjol.charmony.api.StoneCircleDefinition;
import svenhjol.charmony.api.StoneCircleDefinitionProvider;
import svenhjol.charmony.core.Api;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.RunestonesMod;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class StoneCircleDefinitions extends Setup<StoneCircles> implements StoneCircleDefinitionProvider {
    public static final String DEFAULT = "stone";

    public StoneCircleDefinitions(StoneCircles feature) {
        super(feature);
        Api.registerProvider(this);
    }

    @Override
    public List<StoneCircleDefinition> getStoneCircleDefinitions() {
        return List.of(
            stoneCirclesForOverworld(),
            overgrownStoneCirclesForOverworld(),
            stoneCirclesForTheNether(),
            stoneCirclesForTheEnd());
    }

    /**
     * Definition for stone circles that generate throughout the Overworld, generally in flat biomes.
     * The pillars are made from blocks defined in the `stone_pillar_blocks` block tag file.
     * The biomes that these circles may generate in are defined in the `stone_circle_stone` biomes tag file.
     *
     * @return Stone circle definition.
     */
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

            @Override
            public double debrisChance() {
                return feature().handlers.getDebrisChance(StoneCircleDefinition.super.debrisChance());
            }

            @Override
            public List<ResourceKey<LootTable>> archaeologyLootTables() {
                return feature().handlers.getOverworldArchaeologyLoot(StoneCircleDefinition.super.archaeologyLootTables());
            }
        };
    }

    /**
     * Definition for stone circles that generate in forested biomes in the Overworld.
     * The pillars are made from blocks defined in the `overgrown_stone_pillar_blocks` block tag file.
     * The biomes that these circles may generate in are defined in the `stone_circle_overgrown_stone` biomes tag file.
     *
     * @return Stone circle definition.
     */
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

            @Override
            public double debrisChance() {
                return feature().handlers.getDebrisChance(StoneCircleDefinition.super.debrisChance());
            }

            @Override
            public List<ResourceKey<LootTable>> archaeologyLootTables() {
                return feature().handlers.getOverworldArchaeologyLoot(StoneCircleDefinition.super.archaeologyLootTables());
            }
        };
    }

    /**
     * Definition for stone circles that generate throughout the Nether.
     * The pillars are made from blocks defined in the `blackstone_pillar_blocks` block tag file.
     *
     * @return Stone circle definition.
     */
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

            @Override
            public double debrisChance() {
                return feature().handlers.getDebrisChance(StoneCircleDefinition.super.debrisChance());
            }

            @Override
            public List<ResourceKey<LootTable>> archaeologyLootTables() {
                return List.of(); // Nether doesn't have any archaeology.
            }
        };
    }

    /**
     * Definition for stone circles that generate throughout the End dimension.
     * The pillars are made from blocks defined in the `obsidian_pillar_blocks` block tag file.
     *
     * @return Stone circle definition.
     */
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

            @Override
            public double debrisChance() {
                return feature().handlers.getDebrisChance(StoneCircleDefinition.super.debrisChance());
            }

            @Override
            public List<ResourceKey<LootTable>> archaeologyLootTables() {
                return List.of(); // The End doesn't have any archaeology.
            }
        };
    }
}
