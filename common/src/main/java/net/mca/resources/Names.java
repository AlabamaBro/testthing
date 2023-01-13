package net.mca.resources;

import com.google.gson.JsonElement;
import net.mca.Config;
import net.mca.MCA;
import net.mca.entity.ai.relationship.Gender;
import net.mca.server.world.data.Nationality;
import net.minecraft.entity.Entity;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Names extends JsonDataLoader {
    protected static final Identifier ID = MCA.locate("names");

    public static final Map<String, Map<Gender, WeightedPool<String>>> NAMES_MAP = new HashMap<>();
    public static final List<String> REGION_NAMES = new LinkedList<>();

    public Names() {
        super(Resources.GSON, ID.getPath());
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        NAMES_MAP.clear();
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            String[] split = entry.getKey().getPath().split("/");
            Gender gender = Gender.byName(split[1]);

            Map<Gender, WeightedPool<String>> map = NAMES_MAP.computeIfAbsent(split[0], (a) -> new HashMap<>());

            WeightedPool.Mutable<String> names = new WeightedPool.Mutable<>("?");
            for (Map.Entry<String, JsonElement> elementEntry : entry.getValue().getAsJsonObject().entrySet()) {
                names.add(elementEntry.getKey(), (float)Math.pow(elementEntry.getValue().getAsInt(), 0.5));
            }

            map.put(gender, names);
        }

        REGION_NAMES.clear();
        Arrays.stream(NAMES_MAP.keySet().toArray()).sorted().forEach(n -> REGION_NAMES.add((String)n));
    }

    static Random random = new Random();

    public static String getCitizenNation(Entity entity) {
        if (Config.getInstance().useModernUSANamesOnly) {
            return "modernusa";
        } else {
            int i = Nationality.get((ServerWorld)entity.getWorld()).getRegionId(entity.getBlockPos());
            return REGION_NAMES.get(Math.floorMod(i, REGION_NAMES.size()));
        }
    }

    public static String pickCitizenName(@NotNull Gender gender, Entity entity) {
        return NAMES_MAP.get(getCitizenNation(entity)).get(gender.binary()).pickOne();
    }

    public static String pickCitizenName(@NotNull Gender gender) {
        return NAMES_MAP.get(REGION_NAMES.get(random.nextInt(REGION_NAMES.size()))).get(gender.binary()).pickOne();
    }
}
