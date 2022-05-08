package mca.client.model;

import mca.client.render.PlayerEntityMCARenderer;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public interface CommonVillagerModel<T extends LivingEntity> {
    ModelPart getBreastPart();

    ModelPart getBodyPart();

    Iterable<ModelPart> getCommonHeadParts();

    Iterable<ModelPart> getCommonBodyParts();

    Iterable<ModelPart> getBreastParts();

    VillagerDimensions.Mutable getDimensions();

    float getBreastSize();

    void setBreastSize(float getBreastSize);

    default void renderCommon(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        //head
        float headSize = getDimensions().getHead();

        matrices.push();
        matrices.scale(headSize, headSize, headSize);
        getCommonHeadParts().forEach(a -> a.render(matrices, vertices, light, overlay, red, green, blue, alpha));
        matrices.pop();

        //body
        getCommonBodyParts().forEach(a -> a.render(matrices, vertices, light, overlay, red, green, blue, alpha));

        if (getBreastPart().visible && getBodyPart().visible) {
            float breastSize = getBreastSize() * getDimensions().getBreasts();

            if (breastSize > 0) {
                matrices.push();
                matrices.scale(breastSize * 0.2f + 1.05f, breastSize * 0.75f + 0.75f, breastSize * 0.75f + 0.75f);
                for (ModelPart part : getBreastParts()) {
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
                matrices.pop();
            }
        }
    }

    default void applyVillagerDimensions(VillagerLike<?> villager, boolean isSneaking) {
        getDimensions().set(villager.getVillagerDimensions());
        setBreastSize(villager.getGenetics().getBreastSize());
        getBreastPart().visible = villager.getGenetics().getGender() == Gender.FEMALE;

        for (ModelPart part : getBreastParts()) {
            part.pitch = (float)Math.PI * 0.3f + getBodyPart().pitch;

            float cy = 0.0f;
            float cz = 0.0f;
            if (isSneaking) {
                cy = 3.0f;
                cz = 1.5f;
            }

            part.setPivot(0.25f, (float)(5.0f - Math.pow(getBreastSize(), 0.5) * 2.5f + cy), -1.5f + getBreastSize() * 0.25f + cz);
        }
    }

    default void copyCommonAttributes(CommonVillagerModel<T> target) {
        target.getDimensions().set(getDimensions());
        target.setBreastSize(getBreastSize());
    }

    static VillagerLike<?> getVillager(Entity villager) {
        if (villager instanceof VillagerLike<?> v) {
            return v;
        } else {
            return PlayerEntityMCARenderer.playerData.get(villager.getUuid());
        }
    }
}
