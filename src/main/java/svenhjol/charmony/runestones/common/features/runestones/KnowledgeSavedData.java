package svenhjol.charmony.runestones.common.features.runestones;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.charmony.runestones.RunestonesMod;

import java.util.*;

public class KnowledgeSavedData extends SavedData {
    public static final String KNOWLEDGE_TAG = "knowledge";

    private static final Factory<KnowledgeSavedData> FACTORY = new Factory<>(
        KnowledgeSavedData::new,
        KnowledgeSavedData::create,
        null
    );

    private final List<Knowledge> knowledge = new ArrayList<>();

    private static KnowledgeSavedData create(CompoundTag tag, HolderLookup.Provider provider) {
        var state = new KnowledgeSavedData();

        var list = tag.getList(KNOWLEDGE_TAG, 10).stream()
            .map(t -> (CompoundTag)t)
            .toList();

        for (var entry : list) {
            var knowledge = Knowledge.load(entry);
            state.updateKnowledge(knowledge);
        }

        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var list = new ListTag();

        for (var knowledge : knowledge) {
            list.add(knowledge.save());
        }

        tag.put(KNOWLEDGE_TAG, list);
        return tag;
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
        var state = storage.computeIfAbsent(FACTORY, RunestonesMod.ID);
        state.setDirty();
        return state;
    }
}
