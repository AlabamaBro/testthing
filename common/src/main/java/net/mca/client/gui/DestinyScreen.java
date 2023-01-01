package net.mca.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mca.Config;
import net.mca.MCA;
import net.mca.MCAClient;
import net.mca.cobalt.network.NetworkHandler;
import net.mca.network.c2s.DestinyMessage;
import net.mca.util.compat.ButtonWidget;
import net.mca.util.localization.FlowingText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DestinyScreen extends VillagerEditorScreen {
    private static final Identifier LOGO_TEXTURE = new Identifier("mca:textures/banner.png");
    private final LinkedList<Text> story = new LinkedList<>();
    private String location;
    private boolean teleported = false;
    private final boolean allowTeleportation;
    private ButtonWidget acceptWidget;

    public DestinyScreen(UUID playerUUID, boolean allowTeleportation) {
        super(playerUUID, playerUUID);

        this.allowTeleportation = allowTeleportation;
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void close() {
        if (!page.equals("general") && !page.equals("story")) {
            setPage("destiny");
        }
    }

    @Override
    protected String[] getPages() {
        return new String[] {"general", "body", "head", "traits"};
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        assert MinecraftClient.getInstance().world != null;
        renderBackgroundTexture((int)MinecraftClient.getInstance().world.getTime());
    }

    private void drawScaledText(MatrixStack transform, Text text, int x, int y, float scale) {
        transform.push();
        transform.scale(scale, scale, scale);
        drawCenteredText(transform, textRenderer, text, (int)(x / scale), (int)(y / scale), 0xffffffff);
        transform.pop();
    }

    @Override
    public void render(MatrixStack transform, int mouseX, int mouseY, float delta) {
        super.render(transform, mouseX, mouseY, delta);

        switch (page) {
            case "general" -> {
                drawScaledText(transform, Text.translatable("gui.destiny.whoareyou"), width / 2, height / 2 - 24, 1.5f);
                transform.push();
                transform.scale(0.25f, 0.25f, 0.25f);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.setShaderTexture(0, LOGO_TEXTURE);
                DrawableHelper.drawTexture(transform, width * 2 - 512, -40, 0, 0, 1024, 512, 1024, 512);
                transform.pop();
            }
            case "destiny" -> drawScaledText(transform, Text.translatable("gui.destiny.journey"), width / 2, height / 2 - 48, 1.5f);
            case "story" -> {
                List<Text> text = FlowingText.wrap(story.getFirst(), 256);
                int y = (int)(height / 2 - 20 - 7.5f * text.size());
                for (Text t : text) {
                    drawScaledText(transform, t, width / 2, y, 1.25f);
                    y += 15;
                }
            }
        }
    }

    @Override
    protected boolean shouldDrawEntity() {
        return !page.equals("general") && !page.equals("destiny") && !page.equals("story") && super.shouldDrawEntity();
    }

    @Override
    protected void setPage(String page) {
        if (page.equals("destiny") && !allowTeleportation) {
            NetworkHandler.sendToServer(new DestinyMessage(true));
            MCAClient.getDestinyManager().allowClosing();
            super.close();
            return;
        } else if (page.equals("destiny")) {
            //there is only one entry
            if (Config.getInstance().destinyLocations.size() == 1) {
                selectStory(Config.getInstance().destinyLocations.get(0));
                return;
            }
        }

        this.page = page;
        clearChildren();
        switch (page) {
            case "general" -> {
                drawName(width / 2 - DATA_WIDTH / 2, height / 2, name -> {
                    this.updateName(name);
                    if (acceptWidget != null) {
                        acceptWidget.active = !MCA.isBlankString(name);
                    }
                });
                drawGender(width / 2 - DATA_WIDTH / 2, height / 2 + 24);

                drawModel(width / 2 - DATA_WIDTH / 2, height / 2 + 24 + 22);

                acceptWidget = addDrawableChild(new ButtonWidget(width / 2 - 32, height / 2 + 60 + 22, 64, 20, Text.translatable("gui.button.accept"), sender -> {
                    setPage("body");
                }));
            }
            case "destiny" -> {
                int x = 0;
                int y = 0;
                for (String location : Config.getInstance().destinyLocations) {
                    int rows = (int)Math.ceil(Config.getInstance().destinyLocations.size() / 3.0f);
                    float offsetX = (y + 1) == rows ? (2 - (Config.getInstance().destinyLocations.size() - 1) % 3) / 2.0f : 0;
                    float offsetY = Math.max(0, 3 - rows) / 2.0f;
                    addDrawableChild(new ButtonWidget((int)(width / 2 - 96 * 1.5f + (x + offsetX) * 96), (int)(height / 2 + (y + offsetY) * 20 - 16), 96, 20, Text.translatable("gui.destiny." + location), sender -> {
                        selectStory(location);
                    }));
                    x++;
                    if (x >= 3) {
                        x = 0;
                        y++;
                    }
                }
            }
            case "story" -> addDrawableChild(new ButtonWidget(width / 2 - 48, height / 2 + 32, 96, 20, Text.translatable("gui.destiny.next"), sender -> {
                //we teleport early here to avoid initial flickering
                if (!teleported) {
                    NetworkHandler.sendToServer(new DestinyMessage(location));
                    MCAClient.getDestinyManager().allowClosing();
                    teleported = true;
                }
                if (story.size() > 1) {
                    story.remove(0);
                } else {
                    NetworkHandler.sendToServer(new DestinyMessage(true));
                    super.close();
                }
            }));
            default -> super.setPage(page);
        }
    }

    private void selectStory(String location) {
        story.clear();
        story.add(Text.translatable("destiny.story.reason"));
        Map<String, String> map = Config.getInstance().destinyLocationsToTranslationMap;
        story.add(Text.translatable(map.getOrDefault(location, map.getOrDefault("default", "missing_default"))));
        story.add(Text.translatable("destiny.story." + location));
        this.location = location;
        setPage("story");
    }
}
