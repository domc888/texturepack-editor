package dev.packeditor.core;

import dev.packeditor.core.Target.Category;
import java.util.ArrayList;
import java.util.List;

/** All editable targets. Paths are relative to assets/minecraft/. */
public final class Targets {
    private Targets() {}

    public static final List<Target> ALL = List.of(
            // ---- PvP ----
            Target.of("end_crystal", Category.PVP, "End Crystals",
                    "textures/item/end_crystal.png",
                    "textures/entity/end_crystal/end_crystal.png"),
            Target.of("respawn_anchor", Category.PVP, "Respawn Anchors",
                    "textures/block/respawn_anchor_top.png",
                    "textures/block/respawn_anchor_top_off.png",
                    "textures/block/respawn_anchor_bottom.png",
                    "textures/block/respawn_anchor_side0.png",
                    "textures/block/respawn_anchor_side1.png",
                    "textures/block/respawn_anchor_side2.png",
                    "textures/block/respawn_anchor_side3.png",
                    "textures/block/respawn_anchor_side4.png"),
            Target.of("totem", Category.PVP, "Totems",
                    "textures/item/totem_of_undying.png"),
            Target.of("ender_pearl", Category.PVP, "Ender Pearls",
                    "textures/item/ender_pearl.png"),
            Target.of("golden_apple", Category.PVP, "Golden Apples",
                    "textures/item/golden_apple.png",
                    "textures/item/enchanted_golden_apple.png"),
            Target.of("shield", Category.PVP, "Shields",
                    "textures/entity/shield/shield_base_nodeco.png",
                    "textures/entity/shield_base_nodeco.png",
                    "textures/entity/shield/shield_base.png"),
            // ---- Blocks ----
            Target.of("obsidian", Category.BLOCKS, "Obsidian",
                    "textures/block/obsidian.png"),
            Target.of("glowstone", Category.BLOCKS, "Glowstone",
                    "textures/block/glowstone.png"),
            Target.of("cobweb", Category.BLOCKS, "Cobwebs",
                    "textures/block/cobweb.png"),
            Target.of("fire", Category.BLOCKS, "Fire",
                    "textures/block/fire_0.png",
                    "textures/block/fire_1.png"),
            // ---- Gear ----
            Target.of("sword", Category.GEAR, "Swords",
                    "textures/item/diamond_sword.png",
                    "textures/item/netherite_sword.png"),
            Target.of("axe", Category.GEAR, "Axes",
                    "textures/item/diamond_axe.png",
                    "textures/item/netherite_axe.png"),
            Target.of("pickaxe", Category.GEAR, "Pickaxes",
                    "textures/item/diamond_pickaxe.png",
                    "textures/item/netherite_pickaxe.png"),
            Target.of("mace", Category.GEAR, "Mace",
                    "textures/item/mace.png"),
            Target.of("diamond_armour", Category.GEAR, "Diamond Armour",
                    "textures/item/diamond_chestplate.png",
                    "textures/item/diamond_helmet.png",
                    "textures/item/diamond_leggings.png",
                    "textures/item/diamond_boots.png",
                    "textures/entity/equipment/humanoid/diamond.png",
                    "textures/entity/equipment/humanoid_leggings/diamond.png"),
            Target.of("netherite_armour", Category.GEAR, "Netherite Armour",
                    "textures/item/netherite_chestplate.png",
                    "textures/item/netherite_helmet.png",
                    "textures/item/netherite_leggings.png",
                    "textures/item/netherite_boots.png",
                    "textures/entity/equipment/humanoid/netherite.png",
                    "textures/entity/equipment/humanoid_leggings/netherite.png"));

    public static List<Target> in(Category c) {
        List<Target> out = new ArrayList<>();
        for (Target t : ALL) if (t.category() == c) out.add(t);
        return out;
    }

    public static Target byId(String id) {
        for (Target t : ALL) if (t.id().equals(id)) return t;
        return null;
    }
}
