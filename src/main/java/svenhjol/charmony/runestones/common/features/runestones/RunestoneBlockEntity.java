package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import svenhjol.charmony.api.runestones.RunestoneLocation;
import svenhjol.charmony.core.common.SyncedBlockEntity;

import java.util.Optional;

public class RunestoneBlockEntity extends SyncedBlockEntity {
    public static final String LOCATION_TAG = "location";
    public static final String SOURCE_TAG = "source";
    public static final String TARGET_TAG = "target";
    public static final String ITEM_TAG = "item";
    public static final String DISCOVERED_TAG = "discovered";

    public RunestoneLocation location = Helpers.EMPTY_LOCATION;
    public BlockPos source = BlockPos.ZERO;
    public BlockPos target = BlockPos.ZERO;
    public ItemStack item = ItemStack.EMPTY;
    public String discovered = "";
    public int warmup = 0;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.feature().registers.blockEntity.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);

        valueInput.read(LOCATION_TAG, CompoundTag.CODEC).ifPresent(
            val -> this.location = RunestoneLocation.load(val));
        valueInput.getLong(SOURCE_TAG).ifPresent(
            val -> this.source = BlockPos.of(val));
        valueInput.getLong(TARGET_TAG).ifPresent(
            val -> this.target = BlockPos.of(val));
        valueInput.read(ITEM_TAG, ItemStack.CODEC).ifPresentOrElse(
            val -> this.item = val, () -> {
                Item i;
                if (level instanceof ServerLevel serverLevel) {
                    i = Helpers.randomItem(serverLevel, serverLevel.random, "runestone/stone/uncommon");
                } else {
                    i = Items.DIAMOND;
                }
                new ItemStack(i);
            });
        valueInput.getString(DISCOVERED_TAG).ifPresent(
            val -> this.discovered = val);

    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        valueOutput.putLong(SOURCE_TAG, source.asLong());
        valueOutput.putLong(TARGET_TAG, target.asLong());
        valueOutput.putString(DISCOVERED_TAG, discovered);
        valueOutput.store(LOCATION_TAG, CompoundTag.CODEC, location.save());
        valueOutput.store(ITEM_TAG, ItemStack.CODEC, item);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);

        var runestoneData = input.getOrDefault(Runestones.feature().registers.runestoneData.get(), RunestoneData.EMPTY);

        this.location = runestoneData.location();
        this.source = runestoneData.source();
        this.target = runestoneData.target();
        this.item = runestoneData.activate();
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
            this.item,
            this.discovered
        ));
    }

    public void prepare(ServerLevelAccessor level, double quality) {
        Runestones.feature().handlers.prepare(level, getBlockPos(), quality);
    }

    public boolean isValid() {
        return source().isPresent()
            && location().isPresent()
            && item().isPresent();
    }

    public boolean discovered() {
        return !discovered.isEmpty();
    }

    public Optional<RunestoneLocation> location() {
        return location != Helpers.EMPTY_LOCATION ? Optional.of(location) : Optional.empty();
    }

    public Optional<ItemStack> item() {
        return !item.isEmpty() ? Optional.of(item) : Optional.empty();
    }

    public Optional<BlockPos> source() {
        return !source.equals(BlockPos.ZERO) ? Optional.of(source) : Optional.empty();
    }

    public Optional<BlockPos> target() {
        return !target.equals(BlockPos.ZERO) ? Optional.of(target) : Optional.empty();
    }
}
