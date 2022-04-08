package mca.network.s2c;

import mca.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.Serial;
import java.util.UUID;

public class CallToPlayerMessage implements Message.ServerMessage {
    @Serial
    private static final long serialVersionUID = 2556280539773400447L;

    private final UUID uuid;

    public CallToPlayerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Entity e = player.getWorld().getEntity(uuid);
        if (e instanceof VillagerEntityMCA v) {
            v.setPosition(player.getX(), player.getY(), player.getZ());
        }
    }
}