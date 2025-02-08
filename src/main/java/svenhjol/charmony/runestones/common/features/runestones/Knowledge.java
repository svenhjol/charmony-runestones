package svenhjol.charmony.runestones.common.features.runestones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Knowledge(UUID uuid, String name, List<ResourceLocation> locations) {
    public static final Codec<Knowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("uuid").forGetter(Knowledge::uuid),
        Codec.STRING.fieldOf("name").forGetter(Knowledge::name),
        ResourceLocation.CODEC.listOf().fieldOf("locations").forGetter(Knowledge::locations)
    ).apply(instance, Knowledge::new));

    public Knowledge addLocation(ResourceLocation location) {
        var updated = new ArrayList<>(locations());
        if (!updated.contains(location)) {
            updated.add(location);
        }
        return new Knowledge(uuid(), name(), updated);
    }
}
