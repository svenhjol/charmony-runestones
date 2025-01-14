package svenhjol.charmony.runestones.common.features.runestones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import svenhjol.charmony.api.RunestoneLocation;

public record RunestoneData(
    RunestoneLocation location,
    BlockPos source,
    BlockPos target,
    ItemStack sacrifice,
    String discovered
) {
    public static final Codec<RunestoneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CompoundTag.CODEC.fieldOf("location")
            .forGetter(data -> data.location().save()),
        BlockPos.CODEC.fieldOf("source")
            .forGetter(RunestoneData::source),
        BlockPos.CODEC.fieldOf("target")
            .forGetter(RunestoneData::target),
        ItemStack.CODEC.fieldOf("sacrifice")
            .forGetter(RunestoneData::sacrifice),
        Codec.STRING.fieldOf("discovered")
            .forGetter(RunestoneData::discovered)
    ).apply(instance, (tag, source, target, sacrifice, discovered)
        -> new RunestoneData(RunestoneLocation.load(tag), source, target, sacrifice, discovered)));

    public static final StreamCodec<RegistryFriendlyByteBuf, RunestoneData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.COMPOUND_TAG,
            data -> data.location().save(),
        BlockPos.STREAM_CODEC,
            RunestoneData::source,
        BlockPos.STREAM_CODEC,
            RunestoneData::target,
        ItemStack.STREAM_CODEC,
            RunestoneData::sacrifice,
        ByteBufCodecs.STRING_UTF8,
            RunestoneData::discovered,
        (tag, source, target, sacrifice, discovered)
            -> new RunestoneData(RunestoneLocation.load(tag), source, target, sacrifice, discovered));

    public static final RunestoneData EMPTY = new RunestoneData(
        RunestoneHelper.EMPTY_LOCATION,
        BlockPos.ZERO,
        BlockPos.ZERO,
        ItemStack.EMPTY,
        ""
    );
}
