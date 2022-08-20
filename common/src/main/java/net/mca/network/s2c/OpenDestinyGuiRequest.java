package net.mca.network.s2c;

import net.mca.ClientProxy;
import net.mca.Config;
import net.mca.cobalt.network.Message;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenDestinyGuiRequest implements Message {
    private static final long serialVersionUID = -8912548616237596312L;

    public final int player;
    public boolean allowTeleportation;
    public boolean allowPlayerModel;
    public boolean allowVillagerModel;

    public OpenDestinyGuiRequest(ServerPlayerEntity player) {
        this.player = player.getEntityId();

        allowTeleportation = Config.getInstance().allowDestinyTeleportation;
        allowPlayerModel = Config.getInstance().enableVillagerPlayerModel;
        allowVillagerModel = !Config.getInstance().forceVillagerPlayerModel;
    }

    @Override
    public void receive() {
        ClientProxy.getNetworkHandler().handleDestinyGuiRequest(this);
    }
}
