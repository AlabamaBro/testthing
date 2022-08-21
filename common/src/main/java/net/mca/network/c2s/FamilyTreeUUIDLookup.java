package net.mca.network.c2s;

import net.mca.cobalt.network.Message;
import net.mca.cobalt.network.NetworkHandler;
import net.mca.entity.ai.relationship.family.FamilyTree;
import net.mca.entity.ai.relationship.family.FamilyTreeNode;
import net.mca.network.s2c.FamilyTreeUUIDResponse;
import net.mca.resources.data.SerializablePair;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.Serial;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FamilyTreeUUIDLookup implements Message {
    @Serial
    private static final long serialVersionUID = 3458196476082270702L;

    private final String search;

    public FamilyTreeUUIDLookup(String search) {
        this.search = search;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        FamilyTree tree = FamilyTree.get(player.getServerWorld());
        List<SerializablePair<UUID, SerializablePair<String, String>>> list = tree.getAllWithName(search)
                .map(entry -> new SerializablePair<>(entry.id(), new SerializablePair<>(
                        tree.getOrEmpty(entry.father()).map(FamilyTreeNode::getName).orElse(""),
                        tree.getOrEmpty(entry.mother()).map(FamilyTreeNode::getName).orElse(""))))
                .limit(100)
                .collect(Collectors.toList());
        NetworkHandler.sendToPlayer(new FamilyTreeUUIDResponse(list), player);
    }
}
