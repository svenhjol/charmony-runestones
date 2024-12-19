package svenhjol.charmony.runestones.common.features.runestones;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RunestoneBlock extends BaseEntityBlock {
    public static final MapCodec<RunestoneBlock> CODEC = simpleCodec(RunestoneBlock::new);

    public RunestoneBlock(ResourceKey<Block> key) {
        this(Properties.ofFullCopy(Blocks.STONE)
            .pushReaction(PushReaction.IGNORE)
            .setId(key));
    }

    protected RunestoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        if (level.isClientSide()) return null;
        return RunestoneBlock.createTickerHelper(blockEntity,
            Runestones.feature().registers.blockEntity.get(),
            Runestones.feature().handlers::tickRunestone);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        Runestones.feature().handlers.prepareRunestone(level, pos);
    }

    public static class RunestoneBlockItem extends BlockItem {
        public RunestoneBlockItem(Supplier<RunestoneBlock> block, ResourceKey<Item> key) {
            super(block.get(), new Properties().setId(key));
        }
    }
}
