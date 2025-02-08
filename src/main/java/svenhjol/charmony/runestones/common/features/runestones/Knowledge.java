package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public record Knowledge(UUID uuid, String name, List<ResourceLocation> locations) {
    public static final String UUID_TAG = "uuid";
    public static final String NAME_TAG = "name";
    public static final String LOCATIONS_TAG = "locations";

    public CompoundTag save() {
        var tag = new CompoundTag();
        var locationsList = new ListTag();
        for (var entry : locations) {
            locationsList.add(StringTag.valueOf(entry.toString()));
        }
        tag.putUUID(UUID_TAG, uuid());
        tag.putString(NAME_TAG, name());
        tag.put(LOCATIONS_TAG, locationsList);
        return tag;
    }

    public static Knowledge load(CompoundTag tag) {
        var uuid = tag.getUUID(UUID_TAG);
        var name = tag.getString(NAME_TAG);
        var locationStrings = tag.getList(LOCATIONS_TAG, 8).stream()
            .map(Tag::getAsString)
            .toList();

        List<ResourceLocation> locations = new ArrayList<>();

        for (var str : locationStrings) {
            locations.add(ResourceLocation.tryParse(str));
        }

        return new Knowledge(uuid, name, locations);
    }

    public Knowledge addLocation(ResourceLocation location) {
        var updated = new ArrayList<>(locations());
        if (!updated.contains(location)) {
            updated.add(location);
        }
        return new Knowledge(uuid(), name(), updated);
    }
}
