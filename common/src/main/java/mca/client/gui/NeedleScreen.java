package mca.client.gui;

import java.util.UUID;

public class NeedleScreen extends VillagerEditorScreen {
    public NeedleScreen(UUID playerUUID) {
        super(playerUUID, playerUUID);
    }

    public NeedleScreen(UUID villagerUUID, UUID playerUUID) {
        super(villagerUUID, playerUUID);
    }

    @Override
    protected boolean shouldShowPageSelection() {
        return false;
    }

    @Override
    protected void setPage(String page) {
        if (page.equals("loading")) {
            super.setPage("loading");
        } else if (page.equals("body")) {
            syncVillagerData();
            close();
        } else {
            super.setPage("clothing");
        }
    }
}
