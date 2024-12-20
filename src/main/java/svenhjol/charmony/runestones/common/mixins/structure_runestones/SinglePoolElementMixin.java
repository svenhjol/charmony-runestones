package svenhjol.charmony.runestones.common.mixins.structure_runestones;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.charmony.runestones.common.features.structure_runestones.StructureRunestones;

@Mixin(SinglePoolElement.class)
public class SinglePoolElementMixin {
    @Shadow @Final protected Either<ResourceLocation, StructureTemplate> template;

    /**
     * Get a reference to the currently rendering jigsaw template.
     */
    @Inject(
        method = "place",
        at = @At("HEAD")
    )
    private void hookPlace(StructureTemplateManager templateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, RandomSource randomSource, LiquidSettings liquidSettings, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        StructureRunestones.feature().handlers.jigsawTemplate = template.left().orElse(null);
    }
}
