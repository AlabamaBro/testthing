package net.mca.item;

import net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.network.ServerPlayerEntity;

public interface SpecialCaseGift {

    boolean handle(ServerPlayerEntity player, VillagerEntityMCA villager);
}
