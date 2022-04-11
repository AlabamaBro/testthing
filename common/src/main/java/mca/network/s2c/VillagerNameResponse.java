package mca.network.s2c;

import mca.ClientProxy;
import mca.cobalt.network.Message;

import java.io.Serial;

public class VillagerNameResponse implements Message {
    @Serial
    private static final long serialVersionUID = 3907539869834679334L;

    private final String name;

    public VillagerNameResponse(String name) {
        this.name = name;
    }

    @Override
    public void receive() { ClientProxy.getNetworkHandler().handleVillagerNameResponse(this); }

    public String getName() {
        return name;
    }
}
