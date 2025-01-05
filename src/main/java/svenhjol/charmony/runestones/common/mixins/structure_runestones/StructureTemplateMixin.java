package svenhjol.charmony.runestones.common.mixins.structure_runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import svenhjol.charmony.runestones.common.features.structure_runestones.StructureRunestones;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {
    @Unique
    private StructureRunestones feature;

    @Redirect(
        method = "placeInWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/ServerLevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            ordinal = 1
        )
    )
    public boolean hookPlaceInWorld(ServerLevelAccessor level, BlockPos pos, BlockState state, int i) {
        if (feature == null) {
            feature = StructureRunestones.feature(); // Simple cache.
        }
        if (state.is(Blocks.CHISELED_POLISHED_BLACKSTONE) && feature.handlers.createBastionRunestone(level, pos)) {
            return true;
        }
        if (state.is(Blocks.BRICKS) && feature.handlers.createTrailRuinsRunestone(level, pos)) {
            return true;
        }
        return level.setBlock(pos, state, i);
    }
}
