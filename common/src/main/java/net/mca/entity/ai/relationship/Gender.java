package net.mca.entity.ai.relationship;

import net.mca.Config;
import net.mca.entity.EntitiesMCA;
import net.mca.entity.VillagerEntityMCA;
import net.mca.entity.ZombieVillagerEntityMCA;
import net.minecraft.entity.EntityType;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Gender {
    UNASSIGNED(0xFFFFFF, "unassigned"),
    MALE(0x01A6EA, "male"),
    FEMALE(0xA649A4, "female"),
    NEUTRAL(0xFFFFFF, "neutral");

    private static final Random RNG = new Random();
    private static final Gender[] VALUES = values();
    private static final Map<String, Gender> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(Gender::name, Function.identity()));

    private final int color;
    private final String dataName;

    Gender(int color, String dataName) {
        this.color = color;
        this.dataName = dataName;
    }

    public EntityType<VillagerEntityMCA> getVillagerType() {
        return this == FEMALE ? EntitiesMCA.FEMALE_VILLAGER.get() : EntitiesMCA.MALE_VILLAGER.get();
    }

    public EntityType<ZombieVillagerEntityMCA> getZombieType() {
        return this == FEMALE ? EntitiesMCA.FEMALE_ZOMBIE_VILLAGER.get() : EntitiesMCA.MALE_ZOMBIE_VILLAGER.get();
    }

    public int getColor() {
        return color;
    }

    public int getId() {
        return ordinal();
    }

    public String getDataName() {
        return dataName;
    }

    public Gender binary() {
        return this == FEMALE ? FEMALE : MALE;
    }

    public Gender opposite() {
        return this == FEMALE ? MALE : FEMALE;
    }

    public static Gender byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }

    public static Gender getRandom() {
        return RNG.nextBoolean() ? MALE : FEMALE;
    }

    public static Gender byName(String name) {
        return REGISTRY.getOrDefault(name.toUpperCase(Locale.ENGLISH), UNASSIGNED);
    }

    public float getHorizontalScaleFactor() {
        return this == Gender.FEMALE ? Config.getInstance().femaleVillagerWidthFactor :
                this == Gender.MALE ? Config.getInstance().maleVillagerWidthFactor :
                        (Config.getInstance().femaleVillagerWidthFactor + Config.getInstance().maleVillagerWidthFactor) * 0.5f;
    }

    public float getScaleFactor() {
        return this == Gender.FEMALE ? Config.getInstance().femaleVillagerHeightFactor :
                this == Gender.MALE ? Config.getInstance().maleVillagerHeightFactor :
                        (Config.getInstance().femaleVillagerHeightFactor + Config.getInstance().maleVillagerHeightFactor) * 0.5f;
    }
}

