package net.mca.network;

import net.mca.Config;
import net.mca.MCAClient;
import net.mca.client.SpeechManager;
import net.mca.client.book.Book;
import net.mca.client.gui.*;
import net.mca.entity.EntitiesMCA;
import net.mca.entity.VillagerEntityMCA;
import net.mca.entity.VillagerLike;
import net.mca.item.BabyItem;
import net.mca.item.ExtendedWrittenBookItem;
import net.mca.network.s2c.*;
import net.mca.resources.BuildingTypes;
import net.mca.server.world.data.Village;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

public class ClientInteractionManagerImpl implements ClientInteractionManager {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void handleGuiRequest(OpenGuiRequest message) {
        Entity entity;
        assert client.world != null;
        assert MinecraftClient.getInstance().player != null;
        switch (message.getGui()) {
            case WHISTLE:
                client.setScreen(new WhistleScreen());
                break;
            case BOOK:
                if (client.player != null) {
                    ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof ExtendedWrittenBookItem bookItem) {
                        Book book = bookItem.getBook(item);
                        client.setScreen(new ExtendedBookScreen(book));
                    }
                }
                break;
            case BLUEPRINT:
                client.setScreen(new BlueprintScreen());
                break;
            case INTERACT:
                if (client.player != null) {
                    ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                    boolean isOnBlacklist = Config.getInstance().villagerInteractionItemBlacklist.contains(Registry.ITEM.getId(item.getItem()).toString());
                    if (!isOnBlacklist) {
                        VillagerLike<?> villager = (VillagerLike<?>)client.world.getEntityById(message.villager);
                        client.setScreen(new InteractScreen(villager));
                    }
                }
                break;
            case VILLAGER_EDITOR:
                entity = client.world.getEntityById(message.villager);
                assert entity != null;
                client.setScreen(new VillagerEditorScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                break;
            case LIMITED_VILLAGER_EDITOR:
                entity = client.world.getEntityById(message.villager);
                assert entity != null;
                client.setScreen(new LimitedVillagerEditorScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                break;
            case NEEDLE_AND_THREAD:
                entity = client.world.getEntityById(message.villager);
                if (entity == null) {
                    client.setScreen(new NeedleScreen(MinecraftClient.getInstance().player.getUuid()));
                } else {
                    client.setScreen(new NeedleScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                }
                break;
            case COMB:
                entity = client.world.getEntityById(message.villager);
                if (entity == null) {
                    client.setScreen(new CombScreen(MinecraftClient.getInstance().player.getUuid()));
                } else {
                    client.setScreen(new CombScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                }
                break;
            case BABY_NAME:
                if (client.player != null) {
                    ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof BabyItem) {
                        client.setScreen(new NameBabyScreen(client.player, item));
                    }
                }
                break;
            case FAMILY_TREE:
                client.setScreen(new FamilyTreeSearchScreen());
                break;
            default:
        }
    }

    @Override
    public void handleFamilyTreeResponse(GetFamilyTreeResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof FamilyTreeScreen gui) {
            gui.setFamilyData(message.uuid, message.family);
        }
    }

    @Override
    public void handleInteractDataResponse(GetInteractDataResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof InteractScreen gui) {
            gui.setConstraints(message.constraints);
            gui.setParents(message.father, message.mother);
            gui.setSpouse(message.marriageState, message.spouse);
        }
    }

    @Override
    public void handleVillageDataResponse(GetVillageResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof BlueprintScreen gui) {
            BuildingTypes.getInstance().setBuildingTypes(message.buildingTypes);

            Village village = new Village(message.getData(), null);
            gui.setVillage(village);
            gui.setVillageData(message.rank, message.reputation, message.isVillage, message.ids, message.tasks);
        }
    }

    @Override
    public void handleVillageDataFailedResponse(GetVillageFailedResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof BlueprintScreen gui) {
            gui.setVillage(null);
        }
    }

    @Override
    public void handleFamilyDataResponse(GetFamilyResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof WhistleScreen gui) {
            gui.setVillagerData(message.getData());
        }
    }

    @Override
    public void handleVillagerDataResponse(GetVillagerResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof VillagerEditorScreen gui) {
            gui.setVillagerData(message.getData());
        }
    }

    @Override
    public void handleDialogueResponse(InteractionDialogueResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof InteractScreen gui) {
            gui.setDialogue(message.question, message.answers);
        }
    }

    @Override
    public void handleDialogueQuestionResponse(InteractionDialogueQuestionResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof InteractScreen gui) {
            gui.setLastPhrase(message.getQuestionText(), message.silent);
        }
    }

    @Override
    public void handleSkinListResponse(AnalysisResults message) {
        InteractScreen.setAnalysis(message.analysis);
    }

    @Override
    public void handleBabyNameResponse(BabyNameResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof NameBabyScreen gui) {
            gui.setBabyName(message.getName());
        }
    }

    @Override
    public void handleVillagerNameResponse(VillagerNameResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof VillagerEditorScreen gui) {
            gui.setVillagerName(message.getName());
        }
    }

    @Override
    public void handleToastMessage(ShowToastRequest message) {
        SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, message.getTitle(), message.getMessage());
    }

    @Override
    public void handleFamilyTreeUUIDResponse(FamilyTreeUUIDResponse response) {
        Screen screen = client.currentScreen;
        if (screen instanceof FamilyTreeSearchScreen gui) {
            gui.setList(response.getList());
        }
    }

    @Override
    public void handlePlayerDataMessage(PlayerDataMessage response) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(MinecraftClient.getInstance().world);
        assert villager != null;
        villager.readCustomDataFromNbt(response.getData());
        MCAClient.playerData.put(response.uuid, villager);
    }

    @Override
    public void handleSkinListResponse(SkinListResponse message) {
        Screen screen = client.currentScreen;
        VillagerEditorScreen.setSkinList(message.getClothing(), message.getHair());
        if (screen instanceof SkinListUpdateListener gui) {
            gui.skinListUpdatedCallback();
        }
    }

    @Override
    public void handleDestinyGuiRequest(OpenDestinyGuiRequest message) {
        MCAClient.getDestinyManager().requestOpen(message.allowTeleportation);
    }

    @Override
    public void handleConfigResponse(ConfigResponse message) {
        Config.setServerConfig(message.getConfig());
    }

    @Override
    public void handleVillagerMessage(VillagerMessage message) {
        MutableText msg = message.getMessage();
        //noinspection ResultOfMethodCallIgnored
        msg.toString();
        client.getMessageHandler().onGameMessage(msg, false);
        SpeechManager.INSTANCE.onChatMessage(msg, message.getUuid());
    }
}
