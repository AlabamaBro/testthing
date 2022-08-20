package net.mca.network.s2c;

import net.mca.ClientProxy;
import net.mca.network.NbtDataMessage;
import net.mca.server.world.data.BabyTracker.ChildSaveState;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class GetChildDataResponse extends NbtDataMessage {
    private static final long serialVersionUID = -4415670234855916259L;

    public final UUID id;

    public GetChildDataResponse(ChildSaveState data) {
        super(data.writeToNbt(new NbtCompound()));
        this.id = data.getId();
    }

    @Override
    public void receive() {
        ClientProxy.getNetworkHandler().handleChildData(this);
    }
}
