package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.common.SyncedBlockEntity;

import java.util.Optional;

public class RunestoneBlockEntity extends SyncedBlockEntity {
    public static final String LOCATION_TAG = "location";
    public static final String SOURCE_TAG = "source";
    public static final String TARGET_TAG = "target";
    public static final String SACRIFICE_TAG = "sacrifice";
    public static final String DISCOVERED_TAG = "discovered";

    public RunestoneLocation location;
    public BlockPos source;
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
        if (tag.contains(SOURCE_TAG)) {
            this.source = BlockPos.of(tag.getLong(SOURCE_TAG));
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
        if (source != null) {
            tag.putLong(SOURCE_TAG, source.asLong());
        }

        target().ifPresent(pos -> tag.putLong(TARGET_TAG, pos.asLong()));

        if (sacrifice != null) {
            tag.put(SACRIFICE_TAG, sacrifice.save(provider));
        }
        if (discovered != null) {
            tag.putString(DISCOVERED_TAG, discovered);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        var runestoneData = input.getOrDefault(Runestones.feature().registers.runestoneData.get(), RunestoneData.EMPTY);

        this.location = runestoneData.location();
        this.source = runestoneData.source();
        this.target = runestoneData.target();
        this.sacrifice = runestoneData.sacrifice();
        this.discovered = runestoneData.discovered();
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        var data = Runestones.feature().registers.runestoneData.get();

        builder.set(data, new RunestoneData(
            this.location,
            this.source,
            this.target,
            this.sacrifice,
            this.discovered
        ));
    }

    public void prepare(ServerLevelAccessor level, double quality) {
        Runestones.feature().handlers.prepare(level, getBlockPos(), quality);
    }

    public boolean isValid() {
        return source != null && source != BlockPos.ZERO
            && location != null && location != RunestoneHelper.EMPTY_LOCATION
            && sacrifice != null && sacrifice != ItemStack.EMPTY;
    }

    public boolean hasBeenDiscovered() {
        return discovered != null && !discovered.isEmpty();
    }

    public Optional<BlockPos> target() {
        return target != null && target != BlockPos.ZERO ? Optional.of(target) : Optional.empty();
    }
}
