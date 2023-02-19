package net.mca.resources.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mca.MCA;
import net.mca.util.RegistryHelper;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public final class BuildingType implements Serializable {
    @Serial
    private static final long serialVersionUID = 2215455350801127280L;

    private final String name;
    private final int size;
    private final int margin;
    private final String color;
    private final int priority;
    private final boolean visible;
    private final boolean noBeds;
    private final Map<String, Integer> blocks;
    private transient Map<Identifier, Identifier> blockToGroup;
    private transient Map<Identifier, Integer> groups;
    private final boolean icon;
    private final int iconU;
    private final int iconV;
    private final boolean grouped;
    private final int mergeRange;

    public BuildingType() {
        this.name = "?";
        this.size = 0;
        this.margin = 0;
        this.color = "ffffffff";
        this.priority = 0;
        this.visible = true;
        this.noBeds = false;
        this.blocks = Map.of("#minecraft:beds", 1000000000);
        this.blockToGroup = null;
        this.icon = false;
        this.iconU = 0;
        this.iconV = 0;
        this.grouped = false;
        this.mergeRange = 32;
    }

    public BuildingType(String name, JsonObject value) {
        this.name = name;
        this.size = JsonHelper.getInt(value, "size", 0);
        this.margin = JsonHelper.getInt(value, "margin", 0);
        this.color = JsonHelper.getString(value, "color", "ffffffff");
        this.priority = JsonHelper.getInt(value, "priority", 0);
        this.visible = JsonHelper.getBoolean(value, "visible", true);
        this.noBeds = JsonHelper.getBoolean(value, "noBeds", false);

        this.icon = JsonHelper.getBoolean(value, "icon", false);
        this.iconU = JsonHelper.getInt(value, "iconU", 0);
        this.iconV = JsonHelper.getInt(value, "iconV", 0);

        this.grouped = JsonHelper.getBoolean(value, "grouped", false);
        this.mergeRange = JsonHelper.getInt(value, "mergeRange", 0);

        this.blocks = new HashMap<>();
        if (JsonHelper.hasJsonObject(value, "blocks")) {
            JsonObject blocks = JsonHelper.getObject(value, "blocks");
            for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                this.blocks.put(
                        entry.getKey(),
                        entry.getValue().getAsInt()
                );
            }
        }

        this.groups = new HashMap<>();
        if (JsonHelper.hasJsonObject(value, "groups")) {
            JsonObject blocks = JsonHelper.getObject(value, "groups");
            for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                this.groups.put(
                        new Identifier(entry.getKey()),
                        entry.getValue().getAsInt()
                );
            }
        }
    }

    public String name() {
        return name;
    }

    public int size() {
        return size;
    }

    public String color() {
        return color;
    }

    public int priority() {
        return priority;
    }

    public boolean visible() {
        return visible;
    }

    public int getColor() {
        return (int)Long.parseLong(color, 16);
    }

    /**
     * @return a mapping between block identifiers and groups (tags or individual blocks)
     */
    public Map<Identifier, Identifier> getBlockToGroup() {
        if (blockToGroup == null) {
            blockToGroup = new HashMap<>();
            groups = new HashMap<>();
            for (Map.Entry<String, Integer> requirement : blocks.entrySet()) {
                Identifier identifier;
                if (requirement.getKey().startsWith("#")) {
                    identifier = new Identifier(requirement.getKey().substring(1));
                    TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, identifier);
                    if (tag == null || RegistryHelper.isTagEmpty(tag)) {
                        MCA.LOGGER.error("Unknown building type tag " + identifier);
                    } else {
                        var entries = RegistryHelper.getEntries(tag);
                        entries.ifPresent(registryEntries -> {
                            for (Block b : registryEntries.stream().map(RegistryEntry::value).toList()) {
                                blockToGroup.putIfAbsent(Registries.BLOCK.getId(b), identifier);
                            }
                        });
                    }
                } else {
                    identifier = new Identifier(requirement.getKey());
                    blockToGroup.put(identifier, identifier);
                }
                groups.put(identifier, requirement.getValue());
            }
        }
        return blockToGroup;
    }

    public Map<Identifier, Integer> getGroups() {
        getBlockToGroup();
        return groups;
    }

    /**
     * @param blocks the map of block positions per block type of building
     *
     * @return a filtered and grouped map of block types relevant for this building type
     */
    public Map<Identifier, List<BlockPos>> getGroups(Map<Identifier, List<BlockPos>> blocks) {
        HashMap<Identifier, List<BlockPos>> available = new HashMap<>();
        for (Map.Entry<Identifier, List<BlockPos>> entry : blocks.entrySet()) {
            Optional.ofNullable(getBlockToGroup().get(entry.getKey())).ifPresent(v ->
                    available.computeIfAbsent(v, k -> new LinkedList<>()).addAll(entry.getValue())
            );
        }
        return available;
    }

    public boolean isIcon() {
        return icon;
    }

    public int iconU() {
        return iconU * 20;
    }

    public int iconV() {
        return iconV * 60;
    }

    public boolean grouped() {
        return grouped;
    }

    public int mergeRange() {
        return mergeRange;
    }

    public boolean noBeds() {
        return noBeds;
    }

    public int getMargin() {
        return margin;
    }

    public int getMinBlocks() {
        return blocks.values().stream().mapToInt(v -> v).sum();
    }
}
