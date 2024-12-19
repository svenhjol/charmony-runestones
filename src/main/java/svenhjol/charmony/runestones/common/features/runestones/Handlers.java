package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import svenhjol.charmony.api.RunestoneDefinition;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.helper.PlayerHelper;

import java.util.*;

public final class Handlers extends Setup<Runestones> {
    public static final int MAX_WARMUP_TICKS = 8;
    public static final int WARMUP_TICKS = 10;

    public final Map<Block, List<RunestoneDefinition>> definitions = new HashMap<>();
    private final Map<UUID, RunestoneTeleport> activeTeleports = new HashMap<>();

    public Handlers(Runestones feature) {
        super(feature);
    }

    /**
     * Reload all the provider definitions into a map of runestone block -> runestone definition.
     */
    public void serverStart(MinecraftServer server) {
        definitions.clear();
        for (var definition : feature().providers.definitions) {
            this.definitions.computeIfAbsent(definition.runestoneBlock().get(), a -> new ArrayList<>()).add(definition);
        }
    }

    public void playerTick(Player player) {
        var uuid = player.getUUID();

        if (activeTeleports.containsKey(uuid)) {
            var teleport = activeTeleports.get(uuid);
            if (teleport.isValid()) {
                teleport.tick();
            } else {
                log().debug("Removing completed teleport for " + uuid);
                activeTeleports.remove(uuid);
            }
        }
    }

    public void entityJoin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            var serverLevel = (ServerLevel)level;
            var random = RandomSource.create(serverLevel.getSeed());
            Networking.S2CUniqueWorldSeed.send(player, random.nextLong());
        }
    }

    public void tickRunestone(Level level, BlockPos pos, BlockState state, RunestoneBlockEntity runestone) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (runestone.isValid() && level.getGameTime() % WARMUP_TICKS == 0) {
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
                    foundItem.setPickUpDelay(MAX_WARMUP_TICKS * WARMUP_TICKS);

                    // Start the powerup sound.
                    level.playSound(null, pos, feature().registers.powerUpSound.get(), SoundSource.BLOCKS);
                    level.playSound(null, BlockPos.containing(foundItem.position()), feature().registers.fizzleItemSound.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
                }

                var itemPos = foundItem.position();

                // Add particle effect around the item to be consumed. This needs to be done via network packet.
                PlayerHelper.getPlayersInRange(level, pos, 8.0d)
                    .forEach(player -> {
                        // Adds dizziness effect to nearby players.
                        if (!player.hasEffect(MobEffects.CONFUSION)) {
                            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, MAX_WARMUP_TICKS * 20, 20));
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
                    runestone.activate(serverLevel, pos, state);
                }
            } else {
                runestone.warmup = 0;
            }
        }
    }

    /**
     * When a runestone has been activated, all players within range should have an active teleport set.
     */
    public void setActiveTeleport(Player player, RunestoneTeleport teleport) {
        if (player.level().isClientSide()) return;
        activeTeleports.put(player.getUUID(), teleport);
    }

    public void trySetLocation(ServerLevel level, RunestoneBlockEntity runestone) {
        var pos = runestone.getBlockPos();
        var random = RandomSource.create(pos.asLong());
        var target = Helpers.addRandomOffset(level, pos, random, 1000, 2000);
        var registryAccess = level.registryAccess();

        switch (runestone.location.type()) {
            case BIOME -> {
                var result = level.findClosestBiome3d(x -> x.is(runestone.location.id()), target, 6400, 32, 64);
                if (result == null) {
                    log().warn("Could not locate biome for " + runestone.location.id());
                    return;
                }

                runestone.target = result.getFirst();
            }
            case STRUCTURE -> {
                var structureRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
                var opt = structureRegistry.get(runestone.location.id()).map(HolderSet::direct);
                if (opt.isEmpty()) {
                    log().warn("Could not get registered structure for " + runestone.location.id());
                    return;
                }

                var structure = opt.get();
                var result = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, structure, target, 100, false);

                if (result == null) {
                    log().warn("Could not locate structure for " + runestone.location.id());
                    return;
                }

                runestone.target = result.getFirst();
            }
            case PLAYER -> {
                if (Helpers.runestoneLinksToSpawnPoint(runestone)) {
                    runestone.target = null; // Player targets are dynamic.
                }
            }
            default -> {
                log().warn("Not a valid destination type for runestone at " + pos);
                return;
            }
        }

        // Write the target into the runestone.
        runestone.setChanged();
    }

    public void explode(Level level, BlockPos pos) {
        level.explode(null, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 1, Level.ExplosionInteraction.BLOCK);
        level.removeBlock(pos, false);
    }

    public void prepareRunestone(Level level, BlockPos pos) {
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

        var random = RandomSource.create(pos.asLong());
        var blockDefinitions = new ArrayList<>(definitions.get(block));
        Collections.shuffle(blockDefinitions, new Random(random.nextLong()));

        for (var blockDefinition : blockDefinitions) {
            var location = blockDefinition.location(level, pos, random);
            random.nextInt(15335251);

            if (location.isPresent()) {
                var sacrifice = blockDefinition.sacrifice(level, pos, random).get();

                runestone.location = location.get();
                runestone.sacrifice = new ItemStack(sacrifice);

                log().debug("Set runestone location = " + runestone.location.id() + ", sacrifice = " + runestone.sacrifice.toString() + " at pos " + pos);
                runestone.setChanged();
                return;
            }
        }

        // Get a base block from the definitions to replace the runestone with.
        var baseBlock = !blockDefinitions.isEmpty() ? blockDefinitions.getFirst().baseBlock().get() : Blocks.AIR;
        level.setBlock(pos, baseBlock.defaultBlockState(), 2);
        log().debug("Could not resolve a location from runestone at pos " + pos + ", set to base block");
    }
}
