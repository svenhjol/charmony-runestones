package svenhjol.charmony.runestones.common.features.runestones;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import svenhjol.charmony.api.RunestoneDefinition;
import svenhjol.charmony.api.RunestoneDefinitionProvider;
import svenhjol.charmony.core.Api;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.common.CommonRegistry;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.runestones.common.features.runestones.Networking.C2SPlayerLooking;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CActivationWarmup;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CDestroyRunestone;
import svenhjol.charmony.runestones.common.features.runestones.Networking.S2CTeleportedLocation;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneBlock.RunestoneBlockItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class Registers extends Setup<Runestones> {
    public static final String STONE_ID = "stone_runestone";
    public static final String BLACKSTONE_ID = "blackstone_runestone";
    public static final String OBSIDIAN_ID = "obsidian_runestone";

    public final Supplier<DataComponentType<RunestoneData>> runestoneData;

    public final Supplier<BlockEntityType<RunestoneBlockEntity>> blockEntity;

    public final Supplier<RunestoneBlock> stoneBlock;
    public final Supplier<RunestoneBlock> blackstoneBlock;
    public final Supplier<RunestoneBlock> obsidianBlock;

    public final List<Supplier<RunestoneBlockItem>> blockItems = new LinkedList<>(); // to display in order inside creative tab

    public final Supplier<SoundEvent> fizzleItemSound;
    public final Supplier<SoundEvent> powerUpSound;
    public final Supplier<SoundEvent> travelSound;

    public final List<RunestoneDefinition> definitions = new ArrayList<>();

    public Registers(Runestones feature) {
        super(feature);
        var registry = CommonRegistry.forFeature(feature);

        runestoneData = registry.dataComponent("runestone",
            () -> builder -> builder
                .persistent(RunestoneData.CODEC)
                .networkSynchronized(RunestoneData.STREAM_CODEC));

        registry.dataComponentTooltipProvider(runestoneData);

        blockEntity = registry.blockEntity("runestone", () -> RunestoneBlockEntity::new);

        stoneBlock = registry.block(STONE_ID,
            key -> new RunestoneBlock(key, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));
        blackstoneBlock = registry.block(BLACKSTONE_ID,
            key -> new RunestoneBlock(key, BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE)));
        obsidianBlock = registry.block(OBSIDIAN_ID,
            key -> new RunestoneBlock(key, BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)));

        blockItems.add(registry.item(STONE_ID, key -> new RunestoneBlockItem(stoneBlock, key)));
        blockItems.add(registry.item(BLACKSTONE_ID, key -> new RunestoneBlockItem(blackstoneBlock, key)));
        blockItems.add(registry.item(OBSIDIAN_ID, key -> new RunestoneBlockItem(obsidianBlock, key)));

        fizzleItemSound = registry.sound("runestone_fizzle_item");
        powerUpSound = registry.sound("runestone_power_up");
        travelSound = registry.sound("runestone_travel");

        // Server packet senders.
        registry.packetSender(Side.Common, S2CTeleportedLocation.TYPE, S2CTeleportedLocation.CODEC);
        registry.packetSender(Side.Common, S2CActivationWarmup.TYPE, S2CActivationWarmup.CODEC);
        registry.packetSender(Side.Common, S2CDestroyRunestone.TYPE, S2CDestroyRunestone.CODEC);

        // Client packet senders.
        registry.packetSender(Side.Client, C2SPlayerLooking.TYPE, C2SPlayerLooking.CODEC);

        // Handle packets being sent from the client.
        registry.packetReceiver(C2SPlayerLooking.TYPE, () -> feature.handlers::handlePlayerLooking);
    }

    @Override
    public Runnable boot() {
        return () -> {
            var registry = CommonRegistry.forFeature(feature());

            // Consume all RunestoneDefinitions.
            Api.consume(RunestoneDefinitionProvider.class, providers -> {
                for (var definition : providers.getRunestoneDefinitions()) {
                    // Add the block to the runestone block entity.
                    var blockSupplier = definition.runestoneBlock();
                    registry.blocksForBlockEntity(feature().registers.blockEntity, List.of(blockSupplier));

                    // Add the definition to the full set for mapping later.
                    definitions.add(definition);
                }
            });

            ServerLifecycleEvents.SERVER_STARTING.register(feature().handlers::serverStart);
        };
    }
}
