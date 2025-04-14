package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import svenhjol.charmony.api.RunestoneDefinition;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.common.features.teleport.Teleport;
import svenhjol.charmony.core.helpers.PlayerHelper;
import svenhjol.charmony.core.helpers.WorldHelper;
import svenhjol.charmony.rune_dictionary.common.features.rune_dictionary.RuneDictionary;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CDestroyRunestone;

import java.util.*;

@SuppressWarnings("unused")
public class Handlers extends Setup<Runestones> {
    public static final String MULTIPLE_PLAYERS_KEY = "gui.charmony-runestones.runestone.multiple_players";
    public static final int MAX_WARMUP_TICKS = 8;
    public static final int WARMUP_CHECK = 10;

    public final Map<Block, List<RunestoneDefinition>> definitions = new HashMap<>();

    public Handlers(Runestones feature) {
        super(feature);
    }

    /**
     * Reload all the provider definitions into a map of runestone block -> runestone definition.
     */
    public void serverStart(MinecraftServer server) {
        definitions.clear();
        for (var definition : feature().registers.definitions) {
            definitions.computeIfAbsent(definition.runestoneBlock().get(),
                a -> new ArrayList<>()).add(definition);
        }
    }

    public void handlePlayerLooking(Player player, Networking.C2SPlayerLooking payload) {
        var level = player.level();
        var pos = payload.pos();

        // If it's a valid runestone that the player is looking at, do the advancement.
        if (level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone && runestone.isValid()) {
            feature().advancements.lookedAtRunestone(player);
        }
    }

    public void tickRunestone(Level level, BlockPos pos, BlockState state, RunestoneBlockEntity runestone) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (runestone.isValid() && level.getGameTime() % WARMUP_CHECK == 0) {
            ItemEntity foundItem = null;
            var itemEntities = level.getEntitiesOfClass(ItemEntity.class, (new AABB(pos)).inflate(4.8d));
            for (var itemEntity : itemEntities) {
                if (itemEntity.getItem().is(runestone.sacrifice.getItem())) {
                    foundItem = itemEntity;
                    break;
                }
            }
            if (foundItem != null) {
                if (runestone.warmup == 0) {
                    // Don't allow item to be picked up until the ritual is complete...
                    foundItem.setPickUpDelay(MAX_WARMUP_TICKS * WARMUP_CHECK);

                    // Start the powerup sound.
                    level.playSound(null, pos, feature().registers.powerUpSound.get(), SoundSource.BLOCKS);
                    level.playSound(null, BlockPos.containing(foundItem.position()), feature().registers.fizzleItemSound.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
                }

                var itemPos = foundItem.position();

                // Add particle effect around the item to be consumed. This needs to be done via network packet.
                PlayerHelper.getPlayersInRange(level, pos, 8.0d)
                    .forEach(player -> {
                        // Adds dizziness effect to nearby players.
                        if (!player.hasEffect(MobEffects.NAUSEA)) {
                            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, MAX_WARMUP_TICKS * 20, 20));
                        }
                        Networking.S2CActivationWarmup.send((ServerPlayer)player, pos, itemPos);
                    });

                // Increase the number of checks. If maximum, consume the item and activate the runestone.
                runestone.warmup++;
                if (runestone.warmup >= MAX_WARMUP_TICKS) {
                    var stack = foundItem.getItem();
                    if (stack.getCount() > 1) {
                        stack.shrink(1);
                    } else {
                        foundItem.discard();
                    }
                    runestone.warmup = 0;
                    activate(serverLevel, runestone);
                }
            } else {
                runestone.warmup = 0;
            }

            runestone.setChanged();
        }
    }

    public boolean trySetLocation(ServerLevel level, RunestoneBlockEntity runestone) {
        var source = runestone.source().orElse(null);
        if (source == null) {
            return false;
        }

        var random = RandomSource.create(source.asLong());
        var target = Helpers.addRandomOffset(level, source, random, 1000, 2000);
        var registryAccess = level.registryAccess();

        switch (runestone.location.type()) {
            case Biome -> {
                var result = level.findClosestBiome3d(x -> x.is(runestone.location.id()), target, 6400, 32, 64);
                if (result == null) {
                    log().warn("Could not locate biome for " + runestone.location.id());
                    return false;
                }

                runestone.target = result.getFirst();
            }
            case Structure -> {
                var structureRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
                var opt = structureRegistry.get(runestone.location.id()).map(HolderSet::direct);
                if (opt.isEmpty()) {
                    log().warn("Could not get registered structure for " + runestone.location.id());
                    return false;
                }

                var structure = opt.get();
                var result = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, structure, target, 100, false);

                if (result == null) {
                    log().warn("Could not locate structure for " + runestone.location.id());
                    return false;
                }

                runestone.target = result.getFirst();
            }
            case Player -> {
                return true;
            }
            default -> {
                log().warn("Not a valid destination type for runestone with source " + source);
                return false;
            }
        }

        // Write the target into the runestone.
        runestone.setChanged();
        return true;
    }

    public void explode(ServerLevel level, BlockPos pos) {
        var x = pos.getX() + 0.5d;
        var y = pos.getY() + 0.5d;
        var z = pos.getZ() + 0.5d;
        level.explode(null, x, y, z, 1, Level.ExplosionInteraction.BLOCK);

        for (var player : PlayerHelper.getPlayersInRange(level, pos, 8.0d)) {
            S2CDestroyRunestone.send((ServerPlayer)player, pos);
        }

        level.removeBlock(pos, false);
    }

    public void prepare(LevelAccessor level, BlockPos pos, double quality) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone)) {
            return;
        }

        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock block)) {
            return;
        }

        if (!definitions.containsKey(block)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            log().error("No definition found for runestone block " + block + " at pos " + pos);
            return;
        }

        var seed = WorldHelper.seedFromBlockPos(pos);
        var random = RandomSource.create(seed);

        var cardinals = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        level.setBlock(pos, state.setValue(RunestoneBlock.FACING, cardinals.get(random.nextInt(cardinals.size()))), 2);

        var blockDefinitions = new ArrayList<>(definitions.get(block));
        Collections.shuffle(blockDefinitions, new Random(random.nextLong()));

        for (var blockDefinition : blockDefinitions) {
            var location = blockDefinition.location(level, pos, random, quality);
            random.nextInt(15335251);

            if (location.isPresent()) {
                var sacrifice = blockDefinition.sacrifice(level, pos, random, quality).get();

                runestone.source = pos;
                runestone.location = location.get();
                runestone.sacrifice = new ItemStack(sacrifice);

                log().debug("Set runestone location = " + runestone.location.id() + ", sacrifice = " + runestone.sacrifice.toString() + ", quality = " + quality + " at pos " + pos);
                runestone.setChanged();
                return;
            }
        }

        // Get a base block from the definitions to replace the runestone with.
        var baseBlock = !blockDefinitions.isEmpty() ? blockDefinitions.getFirst().baseBlock().get() : Blocks.AIR;
        level.setBlock(pos, baseBlock.defaultBlockState(), 2);
        log().debug("Could not resolve a location from runestone at pos " + pos + ", set to base block");
    }

    /**
     * Teleport all players around the runestone.
     */
    public void activate(ServerLevel level, RunestoneBlockEntity runestone) {
        var feature = Runestones.feature();
        var pos = runestone.getBlockPos();
        var players = PlayerHelper.getPlayersInRange(level, pos, 8.0d);
        var canAddKnowledge = false;

        if (!runestone.discovered()) {
            var result = feature.handlers.trySetLocation(level, runestone);

            if (!result) {
                feature.handlers.explode(level, pos);
                return;
            }

            if (players.size() == 1) {
                var player = players.getFirst();
                if (!player.getAbilities().instabuild) {
                    runestone.discovered = player.getScoreboardName();
                }
            } else {
                runestone.discovered = MULTIPLE_PLAYERS_KEY;
            }

            runestone.setChanged();
            canAddKnowledge = true;
        }

        for (var player : players) {
            var serverPlayer = (ServerPlayer)player;

            // Add the knowledge to each player IF the runestone has not been discovered.
            if (canAddKnowledge) {
                var locationId = runestone.location.id();
                RuneDictionary.feature().handlers.learnWord(serverPlayer, locationId);
            }

            // Add a new teleport request for this player.
            var teleport = new RunestoneTeleporter(serverPlayer, runestone);
            Teleport.feature().handlers.addTeleport(player, teleport);
        }
    }
}
