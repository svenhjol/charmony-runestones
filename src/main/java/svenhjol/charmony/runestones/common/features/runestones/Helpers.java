package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.helpers.TagHelper;
import svenhjol.charmony.runestones.RunestonesMod;

import java.util.List;
import java.util.Optional;

public final class Helpers {
    public static final ResourceLocation SPAWN_POINT_ID = RunestonesMod.id("spawn_point");
    public static final ResourceLocation EMPTY_ID = RunestonesMod.id("empty");
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

    /**
     * Get a random runestone sacrifice item from a given item tag.
     */
    public static Item randomItem(LevelAccessor level, RandomSource random, TagKey<Item> tag) {
        var values = TagHelper.getValues(level.registryAccess().lookupOrThrow(Registries.ITEM), tag);
        if (!values.isEmpty()) {
            return values.get(random.nextInt(values.size()));
        }
        return Items.ROTTEN_FLESH;
    }

    public static Item randomItem(LevelAccessor level, RandomSource random, String res) {
        return randomItem(level, random, TagKey.create(Registries.ITEM, RunestonesMod.id(res)));
    }

    public static BlockPos addRandomOffset(Level level, BlockPos pos, RandomSource random, int min, int max) {
        var n = random.nextInt(max - min) + min;
        var e = random.nextInt(max - min) + min;
        var s = random.nextInt(max - min) + min;
        var w = random.nextInt(max - min) + min;

        pos = pos.north(random.nextBoolean() ? n : -n);
        pos = pos.east(random.nextBoolean() ? e : -e);
        pos = pos.south(random.nextBoolean() ? s : -s);
        pos = pos.west(random.nextBoolean() ? w : -w);

        // World border checking
        var border = level.getWorldBorder();
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        if (x < border.getMinX()) {
            pos = new BlockPos((int)border.getMinX(), y, z);
        } else if (x > border.getMaxX()) {
            pos = new BlockPos((int)border.getMaxX(), y, z);
        }
        if (z < border.getMinZ()) {
            pos = new BlockPos(x, y, (int)border.getMinZ());
        } else if (z > border.getMaxZ()) {
            pos = new BlockPos(x, y, (int)border.getMaxZ());
        }

        return pos;
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
    
    public static boolean runestoneLinksToSpawnPoint(RunestoneBlockEntity runestone) {
        return runestone.location != null && runestone.location.id().equals(SPAWN_POINT_ID);
    }
}
