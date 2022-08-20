package net.mca.network.s2c;

import net.mca.ClientProxy;
import net.mca.cobalt.network.Message;
import net.minecraft.entity.Entity;

public class OpenGuiRequest implements Message {
    private static final long serialVersionUID = -2371116419166251497L;

    public final int gui;

    public final int villager;

    public OpenGuiRequest(OpenGuiRequest.Type gui, Entity villager) {
        this(gui, villager.getEntityId());
    }

    public OpenGuiRequest(OpenGuiRequest.Type gui, int villager) {
        this.gui = gui.ordinal();
        this.villager = villager;
    }

    public OpenGuiRequest(OpenGuiRequest.Type gui) {
        this(gui, 0);
    }

    @Override
    public void receive() {
        ClientProxy.getNetworkHandler().handleGuiRequest(this);
    }

    public Type getGui() {
        return Type.values()[gui];
    }

    public enum Type {
        BABY_NAME,
        WHISTLE,
        BLUEPRINT,
        INTERACT,
        VILLAGER_EDITOR,
        LIMITED_VILLAGER_EDITOR,
        BOOK,
        FAMILY_TREE,
        NEEDLE_AND_THREAD,
        COMB,
        CLOSE,
    }
}
