package mca;

import mca.cobalt.registration.Registration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface TagsMCA {
    public interface Blocks {
        Tag<Block> TOMBSTONES = register("tombstones");

        static void bootstrap() {}

        static Tag<Block> register(String path) {
            return Registration.ObjectBuilders.Tags.block(new Identifier(MCA.MOD_ID, path));
        }
    }

    public interface Items {
        Tag<Item> VILLAGER_EGGS = register("villager_eggs");
        Tag<Item> ZOMBIE_EGGS = register("zombie_eggs");
        Tag<Item> VILLAGER_PLANTABLE = register("villager_plantable");

        Tag<Item> BABIES = register("babies");

        static void bootstrap() {}

        static Tag<Item> register(String path) {
            return Registration.ObjectBuilders.Tags.item(new Identifier(MCA.MOD_ID, path));
        }
    }
}
