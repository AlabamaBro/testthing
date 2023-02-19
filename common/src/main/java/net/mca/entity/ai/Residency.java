package net.mca.entity.ai;

import net.mca.Config;
import net.mca.entity.VillagerEntityMCA;
import net.mca.server.world.data.GraveyardManager;
import net.mca.server.world.data.Village;
import net.mca.server.world.data.VillageManager;
import net.mca.util.network.datasync.CDataManager;
import net.mca.util.network.datasync.CDataParameter;
import net.mca.util.network.datasync.CParameter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Villagers need a place to live too.
 */
public class Residency {
    private static final CDataParameter<Integer> VILLAGE = CParameter.create("buildings", -1);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return builder.addAll(VILLAGE);
    }

    private final VillagerEntityMCA entity;

    public Residency(VillagerEntityMCA entity) {
        this.entity = entity;
    }

    public BlockPos getWorkplace() {
        return entity.getBrain()
                .getOptionalMemory(MemoryModuleType.JOB_SITE)
                .map(GlobalPos::getPos)
                .orElse(BlockPos.ORIGIN);
    }

    public void setWorkplace(ServerPlayerEntity player) {
        PointOfInterestStorage pointOfInterestStorage = player.getWorld().getPointOfInterestStorage();
        pointOfInterestStorage.getNearestPosition(PointOfInterestType.UNEMPLOYED.getCompletionCondition(), (a) -> true, entity.getBlockPos(), 8, PointOfInterestStorage.OccupationStatus.HAS_SPACE).ifPresentOrElse(blockPos -> {
                    pointOfInterestStorage.getType(blockPos).ifPresent((pointOfInterestType) -> {
                        pointOfInterestStorage.getPosition(PointOfInterestType.UNEMPLOYED.getCompletionCondition(), (blockPos2) -> {
                            return blockPos2.equals(blockPos);
                        }, blockPos, 1);

                        // Forget current site
                        entity.releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
                        entity.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
                        entity.releaseTicketFor(MemoryModuleType.JOB_SITE);
                        entity.getBrain().forget(MemoryModuleType.JOB_SITE);

                        // Set
                        GlobalPos globalPos = GlobalPos.create(player.getWorld().getRegistryKey(), blockPos);
                        entity.getBrain().remember(MemoryModuleType.JOB_SITE, globalPos);
                        player.getWorld().sendEntityStatus(entity, (byte)14);
                        MinecraftServer minecraftServer = player.getWorld().getServer();
                        Optional.ofNullable(minecraftServer.getWorld(globalPos.getDimension()))
                                .flatMap(world -> world.getPointOfInterestStorage().getType(globalPos.getPos()))
                                .flatMap(poiType -> Registry.VILLAGER_PROFESSION.stream().filter(profession -> profession.getWorkStation() == poiType).findFirst())
                                .ifPresent(profession -> {
                                    entity.setVillagerData(entity.getVillagerData().withProfession(profession));
                                    entity.reinitializeBrain(player.getWorld());
                                });

                        // Success
                        entity.sendChatMessage(player, "interaction.setworkplace.success");
                    });
                },
                () -> {
                    entity.sendChatMessage(player, "interaction.setworkplace.failed");
                });
    }

    public Optional<Village> getHomeVillage() {
        VillageManager manager = VillageManager.get((ServerWorld)entity.world);
        return manager.getOrEmpty(entity.getTrackedValue(VILLAGE));
    }

    /**
     * Joins the closest village, if in range
     */
    public void seekHome() {
        VillageManager manager = VillageManager.get((ServerWorld)entity.world);
        manager.findNearestVillage(entity).ifPresent(v -> {
            leaveHome();
            v.updateResident(entity);
            entity.setTrackedValue(VILLAGE, v.getId());
        });
    }

    public void leaveHome() {
        Optional<Village> village = getHomeVillage();
        village.ifPresent(v -> {
            v.removeResident(entity);
        });
        entity.setTrackedValue(VILLAGE, -1);
    }

    public void tick() {
        if (!entity.requiresHome()) {
            return;
        }

        //report buildings close by
        if (entity.age % 600 == 0 && entity.doesProfessionRequireHome()) {
            Optional<Village> village = getHomeVillage();
            if (village.isEmpty() && Config.getInstance().enableAutoScanByDefault || village.filter(Village::isAutoScan).isPresent()) {
                reportBuildings();
            }

            //seek a home
            if (village.isEmpty()) {
                seekHome();
            }
        }

        //slowly inject village boni
        if (entity.age % 1200 == 0) {
            getHomeVillage().ifPresentOrElse(village -> {
                //fetch mood from the village storage
                int mood = village.popMood();
                if (mood != 0) {
                    entity.getVillagerBrain().modifyMoodValue(mood);
                }

                //fetch hearts
                entity.world.getPlayers().forEach(player -> {
                    int rep = village.popHearts(player);
                    if (rep != 0) {
                        entity.getVillagerBrain().getMemoriesForPlayer(player).modHearts(rep);
                    }
                });

                //update the reputation
                entity.world.getPlayers().forEach(player -> {
                    //currently, only hearts are considered, maybe additional factors can affect that too
                    int hearts = entity.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
                    village.setReputation(player, entity, hearts);
                });
            }, this::leaveHome);
        }
    }

    //report potential buildings within this villagers reach
    private void reportBuildings() {
        VillageManager manager = VillageManager.get((ServerWorld)entity.world);

        //fetch all near POIs
        Stream<BlockPos> stream = ((ServerWorld)entity.world).getPointOfInterestStorage().getPositions(
                PointOfInterestType.ALWAYS_TRUE,
                (p) -> !manager.cache.contains(p),
                entity.getBlockPos(),
                48,
                PointOfInterestStorage.OccupationStatus.ANY);

        //check if it is a building
        stream.forEach(manager::reportBuilding);

        // also add tombstones
        GraveyardManager.get((ServerWorld)entity.world).reportToVillageManager(entity);
    }

    public void setHome(ServerPlayerEntity player) {
        if (!entity.doesProfessionRequireHome() || entity.getDespawnDelay() > 0) {
            entity.sendChatMessage(player, "interaction.sethome.temporary");
        }

        // also trigger a building refresh, because why not
        VillageManager manager = VillageManager.get((ServerWorld)player.world);
        manager.processBuilding(player.getBlockPos(), true, false);

        seekHome();

        //check if a bed can be found
        PointOfInterestStorage pointOfInterestStorage = player.getWorld().getPointOfInterestStorage();
        Optional<BlockPos> position = pointOfInterestStorage.getSortedPositions(PointOfInterestType.HOME.getCompletionCondition(), (p) -> true, player.getBlockPos(), 8, PointOfInterestStorage.OccupationStatus.HAS_SPACE).findAny();
        if (position.isPresent()) {
            entity.sendChatMessage(player, "interaction.sethome.success");

            // Forget the old one
            entity.getBrain().getOptionalMemory(MemoryModuleType.HOME).ifPresent(p -> {
                entity.releaseTicketFor(MemoryModuleType.HOME);
                entity.getBrain().forget(MemoryModuleType.HOME);
            });

            // Remember the new one
            pointOfInterestStorage.getPosition(PointOfInterestType.HOME.getCompletionCondition(), (p) -> true, position.get(), 1);
            entity.getBrain().remember(MemoryModuleType.HOME, GlobalPos.create(entity.world.getRegistryKey(), position.get()));
            entity.getBrain().remember(MemoryModuleTypeMCA.FORCED_HOME.get(), true);

            seekHome();
        } else {
            entity.getBrain().forget(MemoryModuleTypeMCA.FORCED_HOME.get());

            getHomeVillage().map(v -> v.getBuildingAt(entity.getBlockPos())).filter(Optional::isPresent).map(Optional::get).filter(b -> b.getBuildingType().noBeds()).ifPresentOrElse(building -> {
                entity.sendChatMessage(player, "interaction.sethome.bedfail." + building.getBuildingType().name());
            }, () -> {
                entity.sendChatMessage(player, "interaction.sethome.bedfail");
            });
        }
    }

    public Optional<GlobalPos> getHome() {
        return entity.getMCABrain().getOptionalMemory(MemoryModuleType.HOME);
    }

    public void goHome(PlayerEntity player) {
        entity.getVillagerBrain().setMoveState(MoveState.MOVE, player);
        entity.getInteractions().stopInteracting();
        getHome().filter(p -> p.getDimension() == entity.world.getRegistryKey()).ifPresentOrElse(home -> {
            entity.moveTowards(home.getPos());
            entity.sendChatMessage(player, "interaction.gohome.success");
        }, () -> entity.sendChatMessage(player, "interaction.gohome.fail.nohome"));
    }
}
