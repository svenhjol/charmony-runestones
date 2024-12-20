package svenhjol.charmony.runestones.common.mixins.structure_runestones;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.spongepowered.asm.mixin.Mixin;
import svenhjol.charmony.runestones.common.features.structure_runestones.StructureRunestones;

@Mixin(StrongholdPieces.RoomCrossing.class)
public abstract class StrongholdPiecesMixin extends StrongholdPieces.StrongholdPiece {
    protected StrongholdPiecesMixin(StructurePieceType structurePieceType, int i, BoundingBox boundingBox) {
        super(structurePieceType, i, boundingBox);
    }

    @Override
    protected void placeBlock(WorldGenLevel level, BlockState state, int x, int y, int z, BoundingBox boundingBox) {
        if (state.is(Blocks.STONE_BRICKS)
            && StructureRunestones.feature().handlers.createStrongholdRunestone(level, boundingBox, x, y, z, getWorldPos(x, y, z))) {
            return;
        }
        super.placeBlock(level, state, x, y, z, boundingBox);
    }
}
