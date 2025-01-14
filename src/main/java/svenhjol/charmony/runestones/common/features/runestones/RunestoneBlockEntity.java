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

    public RunestoneLocation location = RunestoneHelper.EMPTY_LOCATION;
    public BlockPos source = BlockPos.ZERO;
    public BlockPos target = BlockPos.ZERO;
    public ItemStack sacrifice = ItemStack.EMPTY;
    public String discovered = "";
    public int warmup = 0;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.feature().registers.blockEntity.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        this.location = RunestoneLocation.load(tag.getCompound(LOCATION_TAG));
        this.source = BlockPos.of(tag.getLong(SOURCE_TAG));
        this.target = BlockPos.of(tag.getLong(TARGET_TAG));
        this.sacrifice = ItemStack.parse(provider, tag.getCompound(SACRIFICE_TAG)).orElseThrow();
        this.discovered = tag.getString(DISCOVERED_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put(LOCATION_TAG, location.save());
        tag.putLong(SOURCE_TAG, source.asLong());
        tag.putLong(TARGET_TAG, target.asLong());
        tag.put(SACRIFICE_TAG, sacrifice.save(provider));
        tag.putString(DISCOVERED_TAG, discovered);
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
        return source().isPresent()
            && location().isPresent()
            && sacrifice().isPresent();
    }

    public boolean discovered() {
        return !discovered.isEmpty();
    }

    public Optional<RunestoneLocation> location() {
        return location != RunestoneHelper.EMPTY_LOCATION ? Optional.of(location) : Optional.empty();
    }

    public Optional<ItemStack> sacrifice() {
        return !sacrifice.isEmpty() ? Optional.of(sacrifice) : Optional.empty();
    }

    public Optional<BlockPos> source() {
        return !source.equals(BlockPos.ZERO) ? Optional.of(source) : Optional.empty();
    }

    public Optional<BlockPos> target() {
        return !target.equals(BlockPos.ZERO) ? Optional.of(target) : Optional.empty();
    }
}
