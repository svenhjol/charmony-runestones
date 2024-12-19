package svenhjol.charmony.runestones.common.features.runestones;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.common.CommonRegistry;
import svenhjol.charmony.core.events.PlayerTickCallback;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CActivationWarmup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CTeleportedLocation;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CUniqueWorldSeed;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneBlock.RunestoneBlockItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Registers extends Setup<Runestones> {
    public static final String STONE_ID = "stone_runestone";
    public static final String BLACKSTONE_ID = "blackstone_runestone";
    public static final String OBSIDIAN_ID = "obsidian_runestone";

    public final Supplier<BlockEntityType<RunestoneBlockEntity>> blockEntity;

    public final Supplier<RunestoneBlock> stoneBlock;
    public final Supplier<RunestoneBlock> blackstoneBlock;
    public final Supplier<RunestoneBlock> obsidianBlock;

    public final List<Supplier<RunestoneBlockItem>> blockItems = new ArrayList<>();

    public final Supplier<SoundEvent> fizzleItemSound;
    public final Supplier<SoundEvent> powerUpSound;
    public final Supplier<SoundEvent> travelSound;

    public Registers(Runestones feature) {
        super(feature);
        var registry = CommonRegistry.forFeature(feature);

        blockEntity = registry.blockEntity("runestone", RunestoneBlockEntity::new);

        stoneBlock = registry.block(STONE_ID, RunestoneBlock::new);
        blackstoneBlock = registry.block(BLACKSTONE_ID, RunestoneBlock::new);
        obsidianBlock = registry.block(OBSIDIAN_ID, RunestoneBlock::new);

        blockItems.add(registry.item(STONE_ID, key -> new RunestoneBlockItem(stoneBlock, key)));
        blockItems.add(registry.item(BLACKSTONE_ID, key -> new RunestoneBlockItem(blackstoneBlock, key)));
        blockItems.add(registry.item(OBSIDIAN_ID, key -> new RunestoneBlockItem(obsidianBlock, key)));

        fizzleItemSound = registry.sound("runestone_fizzle_item");
        powerUpSound = registry.sound("runestone_power_up");
        travelSound = registry.sound("runestone_travel");

        // Server packet senders.
        PayloadTypeRegistry.playS2C().register(S2CTeleportedLocation.TYPE, S2CTeleportedLocation.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CActivationWarmup.TYPE, S2CActivationWarmup.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CUniqueWorldSeed.TYPE, S2CUniqueWorldSeed.CODEC);
    }

    @Override
    public Runnable boot() {
        return () -> {
            PlayerTickCallback.EVENT.register(feature().handlers::playerTick);
            ServerEntityEvents.ENTITY_LOAD.register(feature().handlers::entityJoin);
            ServerLifecycleEvents.SERVER_STARTED.register(feature().handlers::serverStart);
        };
    }
}
