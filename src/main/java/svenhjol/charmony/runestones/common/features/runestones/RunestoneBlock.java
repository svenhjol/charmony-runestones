package svenhjol.charmony.runestones.common.features.runestones;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class RunestoneBlock extends BaseEntityBlock {
    public static final MapCodec<RunestoneBlock> CODEC = simpleCodec(RunestoneBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public RunestoneBlock(ResourceKey<Block> key, BlockBehaviour.Properties properties) {
        this(properties.setId(key));
    }

    protected RunestoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState state2, boolean bl) {
        if (feature().harvestable()) {
            var block = state.getBlock();
            var stack = new ItemStack(block);
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RunestoneBlockEntity runestone) {
                stack.applyComponents(runestone.collectComponents());
                var itemEntity = new ItemEntity(level, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, stack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
        super.onRemove(state, level, pos, state2, bl);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var cardinals = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        var random = RandomSource.create(context.getClickedPos().asLong());
        return defaultBlockState().setValue(FACING, cardinals.get(random.nextInt(cardinals.size())));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        if (level.isClientSide()) return null;
        return RunestoneBlock.createTickerHelper(blockEntity,
            feature().registers.blockEntity.get(),
            feature().handlers::tickRunestone);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);

        if (stack.getItem() instanceof RunestoneBlockItem
            && level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone
            && runestone.isValid()) {
            return;
        }
        feature().handlers.prepare(level, pos, 0d);
    }

    public static class RunestoneBlockItem extends BlockItem {
        public RunestoneBlockItem(Supplier<RunestoneBlock> block, ResourceKey<Item> key) {
            super(block.get(), new Properties().setId(key));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (!(level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone)) {
            return;
        }

        if (!runestone.discovered()) {
            return;
        }

        if (random.nextFloat() < 0.15f) {
            return;
        }

        var particle = ParticleTypes.ENCHANT;
        var dist = 2.5d;

        for (var i = 0; i < 5; i++) {
            level.addParticle(particle, pos.getX() + 0.5d, pos.getY() + 1.2d, pos.getZ() + 0.5d,
                (dist / 2) - (random.nextDouble() * dist), random.nextDouble() - 1.65d, (dist / 2) - (random.nextDouble() * dist));
        }
    }

    private Runestones feature() {
        return Runestones.feature();
    }
}
