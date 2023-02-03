package net.mca.forge;

import net.mca.Config;
import net.mca.MCA;
import net.mca.ParticleTypesMCA;
import net.mca.block.BlockEntityTypesMCA;
import net.mca.block.BlocksMCA;
import net.mca.client.gui.MCAScreens;
import net.mca.client.particle.InteractionParticle;
import net.mca.client.render.GrimReaperRenderer;
import net.mca.client.render.TombstoneBlockEntityRenderer;
import net.mca.client.render.VillagerEntityMCARenderer;
import net.mca.client.render.ZombieVillagerEntityMCARenderer;
import net.mca.client.resources.ColorPaletteLoader;
import net.mca.entity.EntitiesMCA;
import net.mca.item.BabyItem;
import net.mca.item.ItemsMCA;
import net.mca.item.SirbenBabyItem;
import net.mca.resources.Supporters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.ZombieVillagerEntityRenderer;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = MCA.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class MCAForgeClient {
    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        new ClientProxyImpl();
        ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(new MCAScreens());
        ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(new ColorPaletteLoader());
        ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(new Supporters());
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        if (Config.getInstance().useSquidwardModels) {
            EntityRenderers.register(EntitiesMCA.MALE_VILLAGER.get(), VillagerEntityRenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_VILLAGER.get(), VillagerEntityRenderer::new);

            EntityRenderers.register(EntitiesMCA.MALE_ZOMBIE_VILLAGER.get(), ZombieVillagerEntityRenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER.get(), ZombieVillagerEntityRenderer::new);
        } else {
            EntityRenderers.register(EntitiesMCA.MALE_VILLAGER.get(), VillagerEntityMCARenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_VILLAGER.get(), VillagerEntityMCARenderer::new);

            EntityRenderers.register(EntitiesMCA.MALE_ZOMBIE_VILLAGER.get(), ZombieVillagerEntityMCARenderer::new);
            EntityRenderers.register(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER.get(), ZombieVillagerEntityMCARenderer::new);
        }

        EntityRenderers.register(EntitiesMCA.GRIM_REAPER.get(), GrimReaperRenderer::new);

        BlockEntityRendererFactories.register(BlockEntityTypesMCA.TOMBSTONE.get(), TombstoneBlockEntityRenderer::new);

        // todo java.util.ConcurrentModificationException occurred in computeIfAbsent
        ModelPredicateProviderRegistry.register(ItemsMCA.BABY_BOY.get(), new Identifier("invalidated"), (stack, world, entity, i) ->
                BabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );
        ModelPredicateProviderRegistry.register(ItemsMCA.BABY_GIRL.get(), new Identifier("invalidated"), (stack, world, entity, i) ->
                BabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );
        ModelPredicateProviderRegistry.register(ItemsMCA.SIRBEN_BABY_BOY.get(), new Identifier("invalidated"), (stack, world, entity, i) ->
                SirbenBabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );
        ModelPredicateProviderRegistry.register(ItemsMCA.SIRBEN_BABY_GIRL.get(), new Identifier("invalidated"), (stack, world, entity, i) ->
                SirbenBabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );

        // TODO: Forge has made this deprecated in 1.19 and we're meant to use `render_type` in the model json
        // - remove and replace at a later date instead, since Fabric still uses the older method afaik.
        //noinspection removal
        RenderLayers.setRenderLayer(BlocksMCA.INFERNAL_FLAME.get(), RenderLayer.getCutout());
    }

    @SubscribeEvent
    public static void onParticleFactoryRegistration(RegisterParticleProvidersEvent event) {
        event.register(ParticleTypesMCA.NEG_INTERACTION.get(), InteractionParticle.Factory::new);
        event.register(ParticleTypesMCA.POS_INTERACTION.get(), InteractionParticle.Factory::new);
    }
}
