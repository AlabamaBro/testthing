package net.mca.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.PersistentState;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface WorldUtils {
    static List<Entity> getCloseEntities(World world, Entity e, double range) {
        Vec3d pos = e.getPos();
        return world.getOtherEntities(e, new Box(pos, pos).expand(range));
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Entity e, double range, Class<T> c) {
        return getCloseEntities(world, e.getPos(), range, c);
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Vec3d pos, double range, Class<T> c) {
        return world.getNonSpectatingEntities(c, new Box(pos, pos).expand(range));
    }

    static <T extends PersistentState> PersistentState.Type<T> getType(ServerWorld world, Function<NbtCompound, T> loader, Function<ServerWorld, T> factory) {
        return new PersistentState.Type<>(
                () -> {
                    return factory.apply(world);
                },
                loader, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES // Yes, wrong fixer but we do not need one and null is invalid
        );
    }

    static <T extends PersistentState> T loadData(ServerWorld world, Function<NbtCompound, T> loader, Function<ServerWorld, T> factory, String dataId) {
        return world.getPersistentStateManager().getOrCreate(getType(world, loader, factory), dataId);
    }

    static <T extends PersistentState> T getData(ServerWorld world, Function<NbtCompound, T> loader, Function<ServerWorld, T> factory, String dataId) {
        return world.getPersistentStateManager().get(getType(world, loader, factory), dataId);
    }

    static void spawnEntity(World world, MobEntity entity, SpawnReason reason) {
        entity.initialize((ServerWorldAccess) world, world.getLocalDifficulty(entity.getBlockPos()), reason, null, null);
        world.spawnEntity(entity);
    }

    //a wrapper for the unnecessary complex query provided by minecraft
    static Optional<BlockPos> getClosestStructurePosition(ServerWorld world, BlockPos center, Identifier structure, int radius) {
        Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Structure feature = registry.get(structure);
        Optional<RegistryEntry.Reference<Structure>> entry = registry.getEntry(registry.getRawId(feature));
        if (entry.isPresent()) {
            RegistryEntryList.Direct<Structure> of = RegistryEntryList.of(entry.get());
            Pair<BlockPos, RegistryEntry<Structure>> pair = world.getChunkManager().getChunkGenerator().locateStructure(world, of, center, radius, false);
            return pair == null ? Optional.empty() : Optional.ofNullable(pair.getFirst());
        } else {
            return Optional.empty();
        }
    }

    static boolean isChunkLoaded(World world, Vec3i pos) {
        return world.isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }
}
