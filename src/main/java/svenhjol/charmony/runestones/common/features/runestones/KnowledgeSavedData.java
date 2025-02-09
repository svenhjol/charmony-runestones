package svenhjol.charmony.runestones.common.features.runestones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import svenhjol.charmony.runestones.RunestonesMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KnowledgeSavedData extends SavedData {
    public static final Codec<KnowledgeSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Knowledge.CODEC.listOf().fieldOf("knowledge").forGetter(data -> data.knowledge)
    ).apply(instance, KnowledgeSavedData::new));

    public static final SavedDataType<KnowledgeSavedData> TYPE = new SavedDataType<>(
        RunestonesMod.ID + "-knowledge",
        KnowledgeSavedData::new,
        CODEC,
        null
    );

    private List<Knowledge> knowledge = new ArrayList<>();

    public KnowledgeSavedData() {
        setDirty();
    }

    private KnowledgeSavedData(List<Knowledge> knowledge) {
        this.knowledge = knowledge;
    }

    public void updateKnowledge(Knowledge updated) {
        var existing = getKnowledgeByUUID(updated.uuid());
        existing.ifPresent(knowledge::remove);

        knowledge.add(updated);
        setDirty();
    }

    public Knowledge getKnowledge(Player player) {
        var uuid = player.getUUID();
        var name = player.getScoreboardName();
        var existing = getKnowledgeByUUID(uuid);
        return existing.orElseGet(() -> new Knowledge(uuid, name, List.of()));
    }

    public Optional<Knowledge> getKnowledgeByUUID(UUID uuid) {
        return knowledge.stream().filter(k -> k.uuid().equals(uuid)).findFirst();
    }

    public static KnowledgeSavedData getServerState(MinecraftServer server) {
        var level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            throw new RuntimeException("Level not available");
        }
        var storage = level.getDataStorage();
        var state = storage.computeIfAbsent(TYPE);
        state.setDirty();
        return state;
    }
}
