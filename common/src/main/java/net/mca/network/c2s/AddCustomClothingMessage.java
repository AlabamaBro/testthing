package net.mca.network.c2s;

import net.mca.cobalt.network.Message;
import net.mca.resources.data.skin.Clothing;
import net.mca.resources.data.skin.Hair;
import net.mca.resources.data.skin.SkinListEntry;
import net.mca.server.world.data.CustomClothingManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.Serial;

public class AddCustomClothingMessage implements Message {
    @Serial
    private static final long serialVersionUID = 4620788389788045910L;

    final SkinListEntry entry;

    public AddCustomClothingMessage(SkinListEntry entry) {
        this.entry = entry;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        if (entry instanceof Clothing clothing) {
            CustomClothingManager.getClothing().addEntry(entry.identifier, clothing);
        } else if (entry instanceof Hair hair) {
            CustomClothingManager.getHair().addEntry(entry.identifier, hair);
        }
    }
}
