package svenhjol.charmony.runestones.common.features.runestones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import svenhjol.charmony.api.RunestoneLocation;

import java.util.function.Consumer;

public record RunestoneData(
    RunestoneLocation location,
    BlockPos source,
    BlockPos target,
    ItemStack sacrifice,
    String discovered
) implements TooltipProvider {
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

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        if (!this.discovered().isEmpty()) {
            consumer.accept(Component.translatable(RunestoneHelper.localeKey(this.location())).withStyle(ChatFormatting.GOLD));
            consumer.accept(Component.translatable("gui.charmony-runestones.runestone.discovered_by", this.discovered()).withStyle(ChatFormatting.GRAY));
        }
        if (!this.sacrifice().isEmpty()) {
            var hoverName = ((MutableComponent)this.sacrifice().getHoverName()).withStyle(ChatFormatting.BLUE);
            consumer.accept(Component.translatable("gui.charmony-runestones.runestone.activate_with_item_name", hoverName));
        }
    }
}
