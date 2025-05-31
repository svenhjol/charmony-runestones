package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.charmony.api.runestones.RunestoneLocation;
import svenhjol.charmony.api.runestones.RunestoneType;
import svenhjol.charmony.core.Charmony;
import svenhjol.charmony.core.helpers.ItemStackHelper;
import svenhjol.charmony.core.helpers.TagHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class Helpers {
    public static final ResourceLocation SPAWN_POINT_ID = Charmony.id("spawn_point");
    public static final ResourceLocation EMPTY_ID = Charmony.id("empty");
    public static final RunestoneLocation SPAWN_POINT = new RunestoneLocation(RunestoneLocation.Type.Player, SPAWN_POINT_ID);
    public static final RunestoneLocation EMPTY_LOCATION = new RunestoneLocation(RunestoneLocation.Type.Player, EMPTY_ID);

    /**
     * Make a random runestone biome location from a given biome tag.
     */
    public static Optional<RunestoneLocation> randomBiome(LevelAccessor level, RandomSource random, TagKey<Biome> tag) {
        var values = getValues(level.registryAccess(), tag);
        if (!values.isEmpty()) {
            var location = new RunestoneLocation(RunestoneLocation.Type.Biome, values.get(random.nextInt(values.size())));
            return Optional.of(location);
        }

        return Optional.empty();
    }

    /**
     * Make a random runestone structure location from a given structure tag.
     */
    public static Optional<RunestoneLocation> randomStructure(LevelAccessor level, RandomSource random, TagKey<Structure> tag) {
        var values = getValues(level.registryAccess(), tag);
        if (!values.isEmpty()) {
            var location = new RunestoneLocation(RunestoneLocation.Type.Structure, values.get(random.nextInt(values.size())));
            return Optional.of(location);
        }

        return Optional.empty();
    }

    public static Item randomItem(LevelAccessor level, RandomSource random, String res) {
        return ItemStackHelper.randomItem(level, random, TagKey.create(Registries.ITEM, Charmony.id(res)), Items.ROTTEN_FLESH);
    }

    /**
     * Get all values of a given tag.
     */
    public static <T> List<ResourceLocation> getValues(RegistryAccess registryAccess, TagKey<T> tag) {
        var registry = registryAccess.lookupOrThrow(tag.registry());
        return TagHelper.getValues(registry, tag)
            .stream().map(registry::getKey).toList();
    }

    public static String localeKey(RunestoneLocation location) {
        var namespace = location.id().getNamespace();
        var path = location.id().getPath();

        return switch (location.type()) {
            case Biome -> "biome." + namespace + "." + path;
            case Structure -> "structure." + namespace + "." + path;
            case Player -> "player." + namespace + "." + path;
        };
    }

    public static Supplier<RunestoneBlock> getBlocksForType(RunestoneType type) {
        var registers = Runestones.feature().registers;

        return switch (type) {
            case Stone -> registers.stoneBlock;
            case Blackstone -> registers.blackstoneBlock;
            case Obsidian -> registers.obsidianBlock;
        };
    }
    
    public static boolean runestoneLinksToSpawnPoint(RunestoneBlockEntity runestone) {
        return runestone.location != null && runestone.location.id().equals(SPAWN_POINT_ID);
    }
}
