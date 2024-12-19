package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.helper.PlayerHelper;

// TODO: This might need to be synced to the client.
public class RunestoneBlockEntity extends BlockEntity {
    public static final String LOCATION_TAG = "location";
    public static final String TARGET_TAG = "target";
    public static final String SACRIFICE_TAG = "sacrifice";
    public static final String DISCOVERED_TAG = "discovered";

    public RunestoneLocation location;
    public BlockPos target;
    public ItemStack sacrifice;
    public String discovered;
    public int warmup = 0;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.feature().registers.blockEntity.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains(LOCATION_TAG)) {
            this.location = RunestoneLocation.load(tag.getCompound(LOCATION_TAG));
        }
        if (tag.contains(TARGET_TAG)) {
            this.target = BlockPos.of(tag.getLong(TARGET_TAG));
        }
        if (tag.contains(SACRIFICE_TAG)) {
            this.sacrifice = ItemStack.parse(provider, tag.getCompound(SACRIFICE_TAG)).orElseThrow();
        }
        if (tag.contains(DISCOVERED_TAG)) {
            this.discovered = tag.getString(DISCOVERED_TAG);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        if (location != null) {
            tag.put(LOCATION_TAG, location.save());
        }
        if (target != null) {
            tag.putLong(TARGET_TAG, target.asLong());
        }
        if (sacrifice != null) {
            tag.put(SACRIFICE_TAG, sacrifice.save(provider));
        }
        if (discovered != null) {
            tag.putString(DISCOVERED_TAG, discovered);
        }
    }

    public boolean isValid() {
        return location != null && sacrifice != null;
    }

    public boolean hasTarget() {
        return target != null;
    }

    /**
     * Teleport all players around the runestone.
     */
    public void activate(ServerLevel level, BlockPos pos, BlockState state) {
        warmup = 0;
        var feature = Runestones.feature();

        if (!hasTarget()) {
            feature.handlers.trySetLocation(level, this);
        }

        if (!hasTarget()) {
            feature.handlers.explode(level, pos);
        }

        var players = PlayerHelper.getPlayersInRange(level, pos, 8.0d);

        // If there's just one player then they're the ones who discovered this.
        if (discovered == null && players.size() == 1) {
            discovered = players.getFirst().getScoreboardName();
            setChanged();
        }

        for (var player : players) {
            var teleport = new RunestoneTeleport((ServerPlayer)player, this);
            feature.handlers.setActiveTeleport(player, teleport);
        }
    }
}
