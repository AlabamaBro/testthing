package net.mca.client.gui;

import net.mca.entity.VillagerLike;
import net.mca.util.localization.FlowingText;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

public class LimitedVillagerEditorScreen extends VillagerEditorScreen {
    public LimitedVillagerEditorScreen(UUID villagerUUID, UUID playerUUID) {
        super(villagerUUID, playerUUID);
    }

    @Override
    protected boolean shouldShowPageSelection() {
        return false;
    }

    @Override
    protected boolean shouldUsePlayerModel() {
        return villagerData.getInt("playerModel") != VillagerLike.PlayerModel.VILLAGER.ordinal();
    }

    @Override
    protected void setPage(String page) {
        this.page = page;

        if (page.equals("general")) {
            int y = height / 2 - 40;

            //name
            drawName(width / 2, y);
            y += 24;

            //which model to use
            if (villagerUUID.equals(playerUUID)) {
                drawModel(width / 2, y);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        int y = height / 2 + 20;
        List<Text> wrap = FlowingText.wrap(Text.translatable("gui.villager_editor.customization_hint"), DATA_WIDTH);
        for (Text text : wrap) {
            drawCenteredText(matrices, textRenderer, text, width / 2 + DATA_WIDTH / 2, y, 0xFFFFFFFF);
            y += 10;
        }
    }
}
