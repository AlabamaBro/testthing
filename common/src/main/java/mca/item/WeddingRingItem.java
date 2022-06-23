package mca.item;

import mca.Config;
import mca.entity.VillagerEntityMCA;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

public class WeddingRingItem extends RelationshipItem {
    public WeddingRingItem(Item.Settings properties) {
        super(properties);
    }

     @Override
    protected float getHeartsRequired() {
        return Config.getInstance().marriageHeartsRequirement;
    }

    @Override
    public boolean handle(ServerPlayerEntity player, VillagerEntityMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get(player);
        String response;

        if (super.handle(player, villager)) {
            return false;
        } else {
            response = "interaction.marry.success";
            playerData.marry(villager);
            villager.getRelationships().marry(player);
            villager.getVillagerBrain().modifyMoodValue(15);
        }

        villager.sendChatMessage(player, response);
        return true;
    }
}
