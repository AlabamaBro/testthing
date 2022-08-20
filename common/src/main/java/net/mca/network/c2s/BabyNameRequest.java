package net.mca.network.c2s;

import net.mca.cobalt.network.Message;
import net.mca.cobalt.network.NetworkHandler;
import net.mca.entity.ai.relationship.Gender;
import net.mca.network.s2c.BabyNameResponse;
import net.mca.resources.Names;
import net.minecraft.server.network.ServerPlayerEntity;

public class BabyNameRequest implements Message {
    private static final long serialVersionUID = 4965378949498898298L;

    private final Gender gender;

    public BabyNameRequest(Gender gender) {
        this.gender = gender;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        String name = Names.pickCitizenName(gender);
        NetworkHandler.sendToPlayer(new BabyNameResponse(name), player);
    }
}
