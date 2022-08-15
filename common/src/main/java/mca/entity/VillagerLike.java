package mca.entity;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import mca.Config;
import mca.MCA;
import mca.entity.ai.DialogueType;
import mca.entity.ai.Genetics;
import mca.entity.ai.Messenger;
import mca.entity.ai.Traits;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.VillagerDimensions;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.entity.interaction.EntityCommandHandler;
import mca.resources.ClothingList;
import mca.resources.HairList;
import mca.resources.Names;
import mca.server.world.data.PlayerSaveData;
import mca.util.network.datasync.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerDataContainer;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public interface VillagerLike<E extends Entity & VillagerLike<E>> extends CTrackedEntity<E>, VillagerDataContainer, Infectable, Messenger {
    CDataParameter<String> VILLAGER_NAME = CParameter.create("villagerName", "");
    CDataParameter<String> CUSTOM_SKIN = CParameter.create("custom_skin", "");
    CDataParameter<String> CLOTHES = CParameter.create("clothes", "");
    CDataParameter<String> HAIR = CParameter.create("hair", "");
    CDataParameter<Float> HAIR_COLOR_RED = CParameter.create("hair_color_red", 0.0f);
    CDataParameter<Float> HAIR_COLOR_GREEN = CParameter.create("hair_color_green", 0.0f);
    CDataParameter<Float> HAIR_COLOR_BLUE = CParameter.create("hair_color_blue", 0.0f);
    CEnumParameter<AgeState> AGE_STATE = CParameter.create("ageState", AgeState.UNASSIGNED);

    UUID SPEED_ID = UUID.fromString("1eaf83ff-7207-5596-c37a-d7a07b3ec4ce");

    static <E extends Entity> CDataManager.Builder<E> createTrackedData(Class<E> type) {
        return new CDataManager.Builder<>(type)
                .addAll(VILLAGER_NAME, CUSTOM_SKIN, CLOTHES, HAIR, HAIR_COLOR_RED, HAIR_COLOR_GREEN, HAIR_COLOR_BLUE, AGE_STATE)
                .add(Genetics::createTrackedData)
                .add(Traits::createTrackedData)
                .add(VillagerBrain::createTrackedData);
    }

    Genetics getGenetics();

    Traits getTraits();

    VillagerBrain<?> getVillagerBrain();

    EntityCommandHandler<?> getInteractions();

    default void initialize(SpawnReason spawnReason) {
        if (spawnReason != SpawnReason.CONVERSION) {
            if (spawnReason != SpawnReason.BREEDING) {
                getGenetics().randomize();
                getTraits().randomize();
            }

            if (getGenetics().getGender() == Gender.UNASSIGNED) {
                getGenetics().setGender(Gender.getRandom());
            }

            if (Strings.isNullOrEmpty(getTrackedValue(VILLAGER_NAME))) {
                setName(Names.pickCitizenName(getGenetics().getGender(), asEntity()));
            }

            initializeSkin();

            getVillagerBrain().randomize();
        }

        asEntity().calculateDimensions();
    }

    @Override
    default boolean isSpeechImpaired() {
        return getInfectionProgress() > BABBLING_THRESHOLD;
    }

    @Override
    default boolean isToYoungToSpeak() {
        return getAgeState() == AgeState.BABY;
    }

    default void setName(String name) {
        setTrackedValue(VILLAGER_NAME, name);
        EntityRelationship.of(asEntity()).ifPresent(relationship -> relationship.getFamilyEntry().setName(name));
    }

    default void setCustomSkin(String name) {
        setTrackedValue(CUSTOM_SKIN, name);
    }

    default void updateCustomSkin() {

    }

    default GameProfile getGameProfile() {
        return null;
    }

    default boolean hasCustomSkin() {
        if (!getTrackedValue(CUSTOM_SKIN).isEmpty() && getGameProfile() != null) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraftClient.getSkinProvider().getTextures(getGameProfile());
            return map.containsKey(MinecraftProfileTexture.Type.SKIN);
        } else {
            return false;
        }
    }

    default boolean canBeAttractedTo(VillagerLike<?> other) {
        return getTraits().hasSameTrait(Traits.Trait.BISEXUAL, other) || getGenetics().getGender().isMutuallyAttracted(other.getGenetics().getGender());
    }

    default boolean canBeAttractedTo(PlayerSaveData other) {
        return getTraits().hasTrait(Traits.Trait.BISEXUAL) || getGenetics().getGender().isMutuallyAttracted(other.getGender());
    }

    default Hand getDominantHand() {
        return getTraits().hasTrait(Traits.Trait.LEFT_HANDED) ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    default Hand getOpposingHand() {
        return getDominantHand() == Hand.OFF_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    default EquipmentSlot getSlotForHand(Hand hand) {
        return hand == Hand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }

    default EquipmentSlot getDominantSlot() {
        return getSlotForHand(getDominantHand());
    }

    default EquipmentSlot getOpposingSlot() {
        return getSlotForHand(getOpposingHand());
    }

    default Identifier getProfessionId() {
        return MCA.locate("none");
    }

    default String getProfessionName() {
        String professionName = (
                getProfessionId().getNamespace().equalsIgnoreCase("minecraft") ?
                        (getProfessionId().getPath().equals("none") ? "mca.none" : getProfessionId().getPath()) :
                        getProfessionId().toString()
        ).replace(":", ".");

        return professionName.isEmpty() ? "mca.none" : professionName;
    }

    default MutableText getProfessionText() {
        return new TranslatableText("entity.minecraft.villager." + getProfessionName());
    }

    default String getClothes() {
        return getTrackedValue(CLOTHES);
    }

    default void setClothes(Identifier clothes) {
        setClothes(clothes.toString());
    }

    default void setClothes(String clothes) {
        setTrackedValue(CLOTHES, clothes);
    }

    default String getHair() {
        return getTrackedValue(HAIR);
    }

    default void setHair(String hair) {
        setTrackedValue(HAIR, hair);
    }

    default void setHairDye(DyeColor color) {
        float[] components = color.getColorComponents().clone();

        float[] dye = getHairDye();
        if (dye[0] > 0.0f) {
            components[0] = components[0] * 0.5f + dye[0] * 0.5f;
            components[1] = components[1] * 0.5f + dye[1] * 0.5f;
            components[2] = components[2] * 0.5f + dye[2] * 0.5f;
        }

        setTrackedValue(HAIR_COLOR_RED, components[0]);
        setTrackedValue(HAIR_COLOR_GREEN, components[1]);
        setTrackedValue(HAIR_COLOR_BLUE, components[2]);
    }

    default void clearHairDye() {
        setTrackedValue(HAIR_COLOR_RED, 0.0f);
        setTrackedValue(HAIR_COLOR_GREEN, 0.0f);
        setTrackedValue(HAIR_COLOR_BLUE, 0.0f);
    }

    default float[] getHairDye() {
        return new float[] {
                getTrackedValue(HAIR_COLOR_RED),
                getTrackedValue(HAIR_COLOR_GREEN),
                getTrackedValue(HAIR_COLOR_BLUE)
        };
    }

    default AgeState getAgeState() {
        return getTrackedValue(AGE_STATE);
    }

    default VillagerDimensions getVillagerDimensions() {
        return getAgeState();
    }

    default void updateSpeed() {
        //set speed
        float speed = getVillagerBrain().getPersonality().getSpeedModifier();

        speed /= (0.9f + getGenetics().getGene(Genetics.WIDTH) * 0.2f);
        speed *= (0.9f + getGenetics().getGene(Genetics.SIZE) * 0.2f);

        speed *= getAgeState().getSpeed();

        EntityAttributeInstance entityAttributeInstance = asEntity().getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (entityAttributeInstance != null) {
            if (entityAttributeInstance.getModifier(SPEED_ID) != null) {
                entityAttributeInstance.removeModifier(SPEED_ID);
            }
            EntityAttributeModifier speedModifier = new EntityAttributeModifier(SPEED_ID, "Speed", speed - 1.0f, EntityAttributeModifier.Operation.MULTIPLY_BASE);
            entityAttributeInstance.addTemporaryModifier(speedModifier);
        }
    }

    default boolean setAgeState(AgeState state) {
        AgeState old = getAgeState();
        if (state == old) {
            return false;
        }

        setTrackedValue(AGE_STATE, state);
        asEntity().calculateDimensions();
        updateSpeed();

        return old != AgeState.UNASSIGNED;
    }

    default float getHorizontalScaleFactor() {
        if (getGenetics() == null || Config.getInstance().useSquidwardModels) {
            return asEntity().isBaby() ? 0.5f : 1.0f;
        } else {
            return Math.min(0.999f, getGenetics().getHorizontalScaleFactor() * getTraits().getHorizontalScaleFactor() * getVillagerDimensions().getWidth() * getGenetics().getGender().getHorizontalScaleFactor());
        }
    }

    default float getRawScaleFactor() {
        if (getGenetics() == null || Config.getInstance().useSquidwardModels) {
            return asEntity().isBaby() ? 0.5f : 1.0f;
        } else {
            return getGenetics().getVerticalScaleFactor() * getTraits().getVerticalScaleFactor() * getVillagerDimensions().getHeight() * getGenetics().getGender().getScaleFactor();
        }
    }

    @Override
    default DialogueType getDialogueType(PlayerEntity receiver) {
        if (!receiver.world.isClient) {
            // age specific
            DialogueType type = DialogueType.fromAge(getAgeState());

            // relationship specific
            if (!receiver.world.isClient) {
                Optional<EntityRelationship> r = EntityRelationship.of(asEntity());
                if (r.isPresent()) {
                    FamilyTreeNode relationship = r.get().getFamilyEntry();
                    if (r.get().isMarriedTo(receiver.getUuid())) {
                        return DialogueType.SPOUSE;
                    } else if (r.get().isEngagedWith(receiver.getUuid())) {
                        return DialogueType.ENGAGED;
                    } else if (relationship.isParent(receiver.getUuid())) {
                        return type.toChild();
                    }
                }
            }

            // also sync with client
            getVillagerBrain().getMemoriesForPlayer(receiver).setDialogueType(type);
        }

        return getVillagerBrain().getMemoriesForPlayer(receiver).getDialogueType();
    }

    default void initializeSkin() {
        randomizeClothes();
        randomizeHair();
    }

    default void randomizeClothes() {
        setClothes(ClothingList.getInstance().getPool(this).pickOne());
    }

    default void randomizeHair() {
        setHair(HairList.getInstance().getPool(getGenetics().getGender()).pickOne());

        //colored hair
        MobEntity entity = asEntity();
        if (entity.getRandom().nextFloat() < Config.getInstance().coloredHairChance) {
            int n = entity.getRandom().nextInt(25);
            int o = DyeColor.values().length;
            int p = n % o;
            int q = (n + 1) % o;
            float r = entity.getRandom().nextFloat();
            float[] fs = SheepEntity.getRgbColor(DyeColor.byId(p));
            float[] gs = SheepEntity.getRgbColor(DyeColor.byId(q));
            setTrackedValue(HAIR_COLOR_RED, fs[0] * (1.0f - r) + gs[0] * r);
            setTrackedValue(HAIR_COLOR_GREEN, fs[1] * (1.0f - r) + gs[1] * r);
            setTrackedValue(HAIR_COLOR_BLUE, fs[2] * (1.0f - r) + gs[2] * r);
        }
    }

    default void validateClothes() {
        if (!asEntity().world.isClient()) {
            if (!ClothingList.getInstance().clothing.containsKey(getClothes())) {
                //try to port from old versions
                if (getClothes() != null) {
                    Identifier identifier = new Identifier(getClothes());
                    String id = identifier.getNamespace() + ":skins/clothing/normal/" + identifier.getPath();
                    if (ClothingList.getInstance().clothing.containsKey(id)) {
                        setClothes(id);
                    } else {
                        MCA.LOGGER.info(String.format("Villagers clothing %s does not exist!", getClothes()));
                        randomizeClothes();
                    }
                } else {
                    MCA.LOGGER.info(String.format("Villagers clothing %s does not exist!", getClothes()));
                    randomizeClothes();
                }
            }

            if (!HairList.getInstance().hair.containsKey(getHair())) {
                MCA.LOGGER.info(String.format("Villagers hair %s does not exist!", getHair()));
                randomizeHair();
            }
        }
    }

    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    default NbtCompound toNbtForConversion(EntityType<?> convertingTo) {
        NbtCompound output = new NbtCompound();
        this.getTypeDataManager().save((E)asEntity(), output);
        return output;
    }

    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    default void readNbtForConversion(EntityType<?> convertingFrom, NbtCompound input) {
        this.getTypeDataManager().load((E)asEntity(), input);
    }

    default void copyVillagerAttributesFrom(VillagerLike<?> other) {
        readNbtForConversion(other.asEntity().getType(), other.toNbtForConversion(asEntity().getType()));
    }

    static VillagerLike<?> toVillager(Entity entity) {
        if (entity instanceof VillagerLike<?>) {
            return (VillagerLike<?>)entity;
        } else if (entity instanceof ServerPlayerEntity playerEntity) {
            NbtCompound villagerData = PlayerSaveData.get(playerEntity).getEntityData();
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(entity.world);
            assert villager != null;
            villager.readCustomDataFromNbt(villagerData);
            return villager;
        } else {
            return null;
        }
    }

    default boolean isHostile() {
        return false;
    }

    default PlayerModel getPlayerModel() {
        return PlayerModel.VILLAGER;
    }

    boolean isBurned();

    default void spawnBurntParticles() {
        Random random = asEntity().getRandom();
        if (random.nextInt(4) == 0) {
            double d = random.nextGaussian() * 0.02;
            double e = random.nextGaussian() * 0.02;
            double f = random.nextGaussian() * 0.02;
            asEntity().world.addParticle(ParticleTypes.SMOKE, asEntity().getParticleX(1.0), asEntity().getRandomBodyY() + 1.0, asEntity().getParticleZ(1.0), d, e, f);
        }
    }

    enum PlayerModel {
        VILLAGER,
        PLAYER,
        VANILLA;

        static final PlayerModel[] VALUES = values();
    }
}
