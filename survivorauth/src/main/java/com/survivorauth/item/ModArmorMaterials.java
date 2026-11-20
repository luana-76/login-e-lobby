package com.survivorauth.item;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.survivorauth.SurvivorAuth;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Materiais das roupas e acessórios. Um material por set (ex.: "cacador") ou por acessório (id do item).
 * A textura de cada um fica em
 *   assets/survivorauth/textures/models/armor/<nome>_layer_1.png  (capacete, colete, botas)
 *   assets/survivorauth/textures/models/armor/<nome>_layer_2.png  (calça)
 *
 * Nível (tier): 0 acessório (quase não protege), 1 rara (parecida com ferro), 2 épica (diamante), 3 lendária.
 */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, SurvivorAuth.MODID);

    private static final Map<String, Holder<ArmorMaterial>> CACHE = new HashMap<>();

    /** Devolve (registrando na primeira vez) o material com esse nome. Chamar só durante o carregamento do mod. */
    public static Holder<ArmorMaterial> get(String name, int tier) {
        return CACHE.computeIfAbsent(name, n -> MATERIALS.register(n, () -> build(n, tier)));
    }

    private static ArmorMaterial build(String name, int tier) {
        // defesa: capacete, calça, colete, botas, corpo (cavalo)
        int[] d = switch (tier) {
            case 0 -> new int[] {1, 1, 1, 1, 1};
            case 1 -> new int[] {2, 5, 6, 2, 9};
            default -> new int[] {3, 6, 8, 3, 11};
        };
        int enchant = switch (tier) {
            case 0 -> 8;
            case 1 -> 12;
            case 2 -> 20;
            default -> 25;
        };
        float toughness = tier == 2 ? 2.0F : tier >= 3 ? 3.0F : 0.0F;
        float knockback = tier >= 3 ? 0.05F : 0.0F;

        EnumMap<ArmorItem.Type, Integer> defense = Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.HELMET, d[0]);
            m.put(ArmorItem.Type.LEGGINGS, d[1]);
            m.put(ArmorItem.Type.CHESTPLATE, d[2]);
            m.put(ArmorItem.Type.BOOTS, d[3]);
            m.put(ArmorItem.Type.BODY, d[4]);
        });
        return new ArmorMaterial(defense, enchant, SoundEvents.ARMOR_EQUIP_NETHERITE,
                () -> Ingredient.of(Items.NETHERITE_INGOT),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, name))),
                toughness, knockback);
    }

    private ModArmorMaterials() {}
}
