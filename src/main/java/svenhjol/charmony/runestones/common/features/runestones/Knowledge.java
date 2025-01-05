package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record Knowledge(UUID uuid, String name, Map<ResourceLocation, Integer> locations) {
    public static final String UUID_TAG = "uuid";
    public static final String NAME_TAG = "name";
    public static final String LOCATIONS_TAG = "locations";

    public CompoundTag save() {
        var tag = new CompoundTag();
        var locationsList = new ListTag();
        for (var entry : locations.entrySet()) {
            var location = entry.getKey();
            var familiarity = entry.getValue();
            var out = StringTag.valueOf(location.toString() + "|" + familiarity);
            locationsList.add(out);
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

        Map<ResourceLocation, Integer> locations = new HashMap<>();

        for (var concat : locationStrings) {
            var split = concat.split("\\|");
            var location = ResourceLocation.tryParse(split[0]);
            var familiarity = Integer.parseInt(split[1]);
            locations.put(location, familiarity);
        }

        return new Knowledge(uuid, name, locations);
    }

    public Knowledge addLocation(ResourceLocation location) {
        var updated = new HashMap<>(locations());
        var familiarity = locations().getOrDefault(location, 0) + 1;

        updated.put(location, familiarity);
        return new Knowledge(uuid(), name(), updated);
    }
}
