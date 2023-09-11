package net.mca.entity;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum EquipmentSet {
    NAKED(null, null, null, null, null, null),

    GUARD_0(Items.IRON_SWORD, null, null, Items.IRON_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
    GUARD_0_LEFT(null, Items.IRON_SWORD, null, Items.IRON_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
    GUARD_1(Items.IRON_SWORD, Items.SHIELD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.IRON_BOOTS),
    GUARD_2(Items.DIAMOND_SWORD, Items.SHIELD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),

    ARCHER_0(Items.BOW, null, null, Items.LEATHER_CHESTPLATE, null, null),
    ARCHER_0_LEFT(null, Items.BOW, null, Items.LEATHER_CHESTPLATE, null, null),
    ARCHER_1(Items.BOW, null, null, Items.IRON_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
    ARCHER_1_LEFT(null, Items.BOW, null, Items.IRON_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
    ARCHER_2(Items.BOW, null, null, Items.DIAMOND_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.IRON_BOOTS),
    ARCHER_2_LEFT(null, Items.BOW, null, Items.DIAMOND_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.IRON_BOOTS),

    ELITE(Items.NETHERITE_SWORD, Items.NETHERITE_SWORD, Items.DIAMOND_HELMET, Items.NETHERITE_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.NETHERITE_BOOTS),
    ROYAL(Items.TRIDENT, Items.DIAMOND_AXE, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);

    EquipmentSet(Item mainHand, Item offHand, Item head, Item chest, Item legs, Item feet) {
        this.mainHand = mainHand;
        this.getOffHand = offHand;
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
    }

    final Item mainHand;
    final Item getOffHand;
    final Item head;
    final Item chest;
    final Item legs;
    final Item feet;

    public Item getMainHand() {
        return mainHand;
    }

    public Item getGetOffHand() {
        return getOffHand;
    }

    public Item getHead() {
        return head;
    }

    public Item getChest() {
        return chest;
    }

    public Item getLegs() {
        return legs;
    }

    public Item getFeet() {
        return feet;
    }
}
