package mca;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface SoundsMCA {
    DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(MCA.MOD_ID, Registry.SOUND_EVENT_KEY);
    
    RegistrySupplier<SoundEvent> REAPER_SCYTHE_OUT = register("reaper.scythe_out");
    RegistrySupplier<SoundEvent> REAPER_SCYTHE_SWING = register("reaper.scythe_swing");
    RegistrySupplier<SoundEvent> REAPER_IDLE = register("reaper.idle");
    RegistrySupplier<SoundEvent> REAPER_DEATH = register("reaper.death");
    RegistrySupplier<SoundEvent> REAPER_BLOCK = register("reaper.block");
    RegistrySupplier<SoundEvent> REAPER_SUMMON = register("reaper.summon");

    RegistrySupplier<SoundEvent> VILLAGER_BABY_LAUGH = register("villager.baby.laugh");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_SCREAM = register("villager.male.scream");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_SCREAM = register("villager.female.scream");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_HURT = register("villager.male.hurt");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_HURT = register("villager.female.hurt");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_LAUGH = register("villager.male.laugh");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_LAUGH = register("villager.female.laugh");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_CRY = register("villager.male.cry");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_CRY = register("villager.female.cry");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_ANGRY = register("villager.male.angry"); // todo
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_ANGRY = register("villager.female.angry"); // todo

    RegistrySupplier<SoundEvent> VILLAGER_MALE_GREET = register("villager.male.greet");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_GREET = register("villager.female.greet");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_SURPRISE = register("villager.male.surprise");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_SURPRISE = register("villager.female.surprise");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_YES = register("villager.male.yes");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_YES = register("villager.female.yes");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_NO = register("villager.male.no");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_NO = register("villager.female.no");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_COUGH = register("villager.male.cough");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_COUGH = register("villager.female.cough");

    RegistrySupplier<SoundEvent> VILLAGER_MALE_SNORE = register("villager.male.snore");
    RegistrySupplier<SoundEvent> VILLAGER_FEMALE_SNORE = register("villager.female.snore");

    RegistrySupplier<SoundEvent> SIRBEN = register("villager.sirben");

    RegistrySupplier<SoundEvent> SILENT = register("silent");

    static void bootstrap() {
        SOUNDS.register();
    }

    static RegistrySupplier<SoundEvent> register(String sound) {
        Identifier id = new Identifier(MCA.MOD_ID, sound);
        return SOUNDS.register(id, () -> new SoundEvent(id));
    }
}
