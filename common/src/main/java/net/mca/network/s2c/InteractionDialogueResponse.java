package net.mca.network.s2c;

import net.mca.ClientProxy;
import net.mca.cobalt.network.Message;
import net.mca.entity.VillagerEntityMCA;
import net.mca.resources.data.dialogue.Question;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class InteractionDialogueResponse implements Message {
    private static final long serialVersionUID = 1371939319244994642L;

    public final String question;
    public final List<String> answers;
    public final boolean silent;

    public InteractionDialogueResponse(Question question, ServerPlayerEntity player, VillagerEntityMCA villager) {
        this.question = question.getId();
        this.answers = question.getValidAnswers(player, villager);
        this.silent = question.isSilent();
    }

    @Override
    public void receive() {
        ClientProxy.getNetworkHandler().handleDialogueResponse(this);
    }
}
