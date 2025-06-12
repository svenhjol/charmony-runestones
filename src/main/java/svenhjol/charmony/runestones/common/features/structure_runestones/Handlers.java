package svenhjol.charmony.runestones.common.features.structure_runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneBlockEntity;
import svenhjol.charmony.runestones.common.features.runestones.Runestones;

import javax.annotation.Nullable;

public class Handlers extends Setup<StructureRunestones> {
    public @Nullable ResourceLocation jigsawTemplate;

    public Handlers(StructureRunestones feature) {
        super(feature);
    }

    public boolean createStrongholdRunestone(ServerLevelAccessor level, BoundingBox boundingBox, int x, int y, int z, BlockPos pos) {
        if (!feature().enabled()) return false;

        var random = level.getRandom();
        if (boundingBox.isInside(pos)
            && y == 2
            && random.nextDouble() < feature().strongholdChance())
        {
            level.setBlock(pos, Runestones.feature().registers.stoneBlock.get().defaultBlockState(), 2);
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RunestoneBlockEntity runestone) {
                runestone.prepare(level, 0.8d);
                return true;
            }
        }
        return false;
    }

    public boolean createBastionRunestone(ServerLevelAccessor level, BlockPos pos) {
        var random = level.getRandom();
        if (jigsawTemplate != null
            && jigsawTemplate.getPath().contains("bastion")
            && random.nextDouble() < feature().bastionChance())
        {
            level.setBlock(pos, Runestones.feature().registers.blackstoneBlock.get().defaultBlockState(), 2);
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RunestoneBlockEntity runestone) {
                runestone.prepare(level, 0.6d);
                return true;
            }
        }
        return false;
    }

    public boolean createTrailRuinsRunestone(ServerLevelAccessor level, BlockPos pos) {
        var random = level.getRandom();
        if (jigsawTemplate != null
            && jigsawTemplate.getPath().contains("trail_ruins")
            && random.nextDouble() < feature().trailRuinsChance())
        {
            level.setBlock(pos, Runestones.feature().registers.stoneBlock.get().defaultBlockState(), 2);
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RunestoneBlockEntity runestone) {
                runestone.prepare(level, 0.2d);
                return true;
            }
        }
        return false;
    }
}
