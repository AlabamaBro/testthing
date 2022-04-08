package mca.network.s2c;

import mca.cobalt.network.Message;
import mca.server.world.data.VillageManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.Serial;

public class RenameVillageMessage implements Message.ServerMessage {
    @Serial
    private static final long serialVersionUID = -7194992618247743620L;

    private final int id;
    private final String name;

    public RenameVillageMessage(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        VillageManager.get(player.getWorld()).getOrEmpty(id).ifPresent(v -> v.setName(name));
    }
}