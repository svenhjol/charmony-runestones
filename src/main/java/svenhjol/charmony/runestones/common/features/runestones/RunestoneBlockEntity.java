package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.common.SyncedBlockEntity;

public class RunestoneBlockEntity extends SyncedBlockEntity {
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

    public void prepare(ServerLevelAccessor level, double quality) {
        Runestones.feature().handlers.prepare(level, getBlockPos(), quality);
    }

    public boolean isValid() {
        return location != null && sacrifice != null;
    }

    public boolean hasBeenDiscovered() {
        return discovered != null;
    }
}
