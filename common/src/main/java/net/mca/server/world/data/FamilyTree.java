package net.mca.server.world.data;

import net.mca.entity.ai.relationship.EntityRelationship;
import net.mca.entity.ai.relationship.Gender;
import net.mca.entity.ai.relationship.RelationshipState;
import net.mca.util.NbtHelper;
import net.mca.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class FamilyTree extends PersistentState {
    private static final String DATA_ID = "MCA-FamilyTree";

    private final Map<UUID, FamilyTreeNode> entries;

    public static FamilyTree get(ServerWorld world) {
        return WorldUtils.loadData(world.getServer().getOverworld(), FamilyTree::new, FamilyTree::new, DATA_ID);
    }

    FamilyTree(ServerWorld world) {
        entries = new HashMap<>();
    }

    FamilyTree(NbtCompound nbt) {
        entries = NbtHelper.toMap(nbt, UUID::fromString, (id, element) -> new FamilyTreeNode(this, id, (NbtCompound)element));

        // Fixing the shift in relationships introduces by the promised update
        // TODO the moment this code here causes any porting errors, remove it. By that time no old saves should exist anyways.
        UUID uuid = UUID.fromString("12341234-1234-1234-1234-123412341234");
        if (!entries.containsKey(uuid)) {
            entries.put(uuid, createEntry(uuid, "debug", Gender.NEUTRAL, false));
            markDirty();

            entries.values().forEach(e -> {
                FamilyTreeNode partner = entries.get(e.partner());
                boolean partnerIsPlayer = partner != null && partner.isPlayer();
                if (e.getRelationshipState() == RelationshipState.ENGAGED && partnerIsPlayer == e.isPlayer()) {
                    //this is a villager-villager or player-player relationship. They do not have engagement
                    e.setRelationshipState(RelationshipState.MARRIED_TO_VILLAGER);
                }
                if (e.getRelationshipState() == RelationshipState.MARRIED_TO_VILLAGER && partnerIsPlayer) {
                    //The partner is not a villager
                    e.setRelationshipState(RelationshipState.MARRIED_TO_PLAYER);
                }
                if (e.getRelationshipState() == RelationshipState.MARRIED_TO_PLAYER && !partnerIsPlayer) {
                    //The partner is not a player
                    e.setRelationshipState(RelationshipState.WIDOW);
                }
            });
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return NbtHelper.fromMap(nbt, entries, UUID::toString, FamilyTreeNode::save);
    }

    public Optional<FamilyTreeNode> getOrEmpty(@Nullable UUID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(entries.get(id));
    }

    public Stream<FamilyTreeNode> getAllWithName(String name) {
        return entries.values().stream().filter(n -> n.getName().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)));
    }

    @NotNull
    public FamilyTreeNode getOrCreate(Entity entity) {
        return entries.computeIfAbsent(entity.getUuid(), uuid -> createEntry(
                entity.getUuid(),
                entity.getName().getString(),
                EntityRelationship.of(entity).map(EntityRelationship::getGender).orElse(Gender.MALE),
                entity instanceof PlayerEntity));
    }

    public void remove(UUID id) {
        entries.remove(id);
        markDirty();
    }

    @NotNull
    public FamilyTreeNode getOrCreate(UUID id, String name, Gender gender) {
        return getOrCreate(id, name, gender, false);
    }

    @NotNull
    public FamilyTreeNode getOrCreate(UUID id, String name, Gender gender, boolean isPlayer) {
        return entries.computeIfAbsent(id, uuid -> createEntry(uuid, name, gender, isPlayer));
    }

    private FamilyTreeNode createEntry(UUID uuid, String name, Gender gender, boolean isPlayer) {
        markDirty();
        return new FamilyTreeNode(this,
                uuid,
                name,
                isPlayer,
                gender,
                Util.NIL_UUID,
                Util.NIL_UUID
        );
    }
}
