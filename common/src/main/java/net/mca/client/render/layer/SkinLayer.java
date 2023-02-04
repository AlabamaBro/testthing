package net.mca.client.render.layer;

import net.mca.MCA;
import net.mca.client.resources.ColorPalette;
import net.mca.entity.ai.Genetics;
import net.mca.entity.ai.Traits;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import static net.mca.client.model.CommonVillagerModel.getVillager;

public class SkinLayer<T extends LivingEntity, M extends BipedEntityModel<T>> extends VillagerLayer<T, M> {
    public SkinLayer(FeatureRendererContext<T, M> renderer, M model) {
        super(renderer, model);
    }

    @Override
    public Identifier getSkin(T villager) {
        Genetics genetics = getVillager(villager).getGenetics();
        int skin = (int) Math.min(4, Math.max(0, genetics.getGene(Genetics.SKIN) * 5));
        return cached(String.format("%s:skins/skin/%s/%d.png", MCA.MOD_ID, genetics.getGender().getStrName(), skin), Identifier::new);
    }

    @Override
    public float[] getColor(T villager, float tickDelta) {
        float albinism = getVillager(villager).getTraits().hasTrait(Traits.Trait.ALBINISM) ? 0.1f : 1.0f;

        return ColorPalette.SKIN.getColor(
                getVillager(villager).getGenetics().getGene(Genetics.MELANIN) * albinism,
                getVillager(villager).getGenetics().getGene(Genetics.HEMOGLOBIN) * albinism,
                getVillager(villager).getInfectionProgress()
        );
    }
}
