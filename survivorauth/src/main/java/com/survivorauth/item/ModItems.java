package com.survivorauth.item;

import com.survivorauth.SurvivorAuth;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Roupas e acessórios vestíveis (usam os slots de armadura). O nome registrado é o MESMO id usado na loja
 * (ShopCatalog), por exemplo "vip_mascara_caveira": é assim que o servidor sabe qual item entregar.
 *
 * Para criar outra peça: adicione uma linha em armor(...), a textura em
 * textures/item/<id>.png, o modelo em models/item/<id>.json, o nome em lang/*.json (item.survivorauth.<id>),
 * a textura de armadura em textures/models/armor/<material>_layer_1.png / _layer_2.png e o item em ShopCatalog.
 * Clothes dividem o material do set (ex.: "cacador"); cada acessório tem material próprio (= id).
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SurvivorAuth.MODID);

    /** Fator de durabilidade por nível: 0 acessório, 1 rara, 2 épica, 3 lendária. */
    private static final int[] DURABILITY = {15, 25, 37, 42};

    static {
        armor("vip_mascara_caveira", "shadow", 3, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_manto_sombrio", "shadow", 3, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_capacete_artico", "arctic", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_colete_artico", "arctic", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_calca_artica", "arctic", 2, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("capuz_cacador", "cacador", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("jaqueta_cacador", "cacador", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_cacador", "cacador", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_cacador", "cacador", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("capacete_operador", "operador", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("colete_operador", "operador", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_operador", "operador", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_operador", "operador", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("mascara_hazmat", "hazmat", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("traje_hazmat", "hazmat", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_hazmat", "hazmat", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_hazmat", "hazmat", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("capacete_motoqueiro", "motoqueiro", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("jaqueta_motoqueiro", "motoqueiro", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_motoqueiro", "motoqueiro", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_motoqueiro", "motoqueiro", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("vip_elmo_real", "realeza", 3, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_casaco_real", "realeza", 3, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_calca_real", "realeza", 3, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("vip_botas_reais", "realeza", 3, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("acc_oculos_aviador", "acc_oculos_aviador", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_oculos_noturno", "acc_oculos_noturno", 0, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("acc_bandana", "acc_bandana", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_bone_tatico", "acc_bone_tatico", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_chapeu_cowboy", "acc_chapeu_cowboy", 0, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("acc_mascara_gas", "acc_mascara_gas", 0, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("acc_headset", "acc_headset", 0, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("vip_balaclava", "vip_balaclava", 0, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_coroa_dourada", "vip_coroa_dourada", 0, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("acc_tapa_olho", "acc_tapa_olho", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_cachecol", "acc_cachecol", 0, ArmorItem.Type.CHESTPLATE, Rarity.COMMON);
        armor("acc_mochila", "acc_mochila", 0, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("acc_luvas", "acc_luvas", 0, ArmorItem.Type.CHESTPLATE, Rarity.COMMON);
        armor("vip_bandoleira", "vip_bandoleira", 0, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("acc_cinto", "acc_cinto", 0, ArmorItem.Type.LEGGINGS, Rarity.COMMON);
        armor("acc_coldre", "acc_coldre", 0, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("vip_joelheiras", "vip_joelheiras", 0, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("acc_botas_combate", "acc_botas_combate", 0, ArmorItem.Type.BOOTS, Rarity.COMMON);
        armor("acc_caneleiras", "acc_caneleiras", 0, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("vip_tenis_neon", "vip_tenis_neon", 0, ArmorItem.Type.BOOTS, Rarity.EPIC);
    }

    private static void armor(String id, String material, int tier, ArmorItem.Type type, Rarity rarity) {
        Holder<ArmorMaterial> mat = ModArmorMaterials.get(material, tier);
        ITEMS.register(id, () -> new ArmorItem(mat, type,
                new Item.Properties().durability(type.getDurability(DURABILITY[tier])).rarity(rarity)));
    }

    /** Item do mod com o id da loja, ou null se esse id da loja não é uma peça vestível. */
    public static ArmorItem byShopId(String id) {
        ResourceLocation key = ResourceLocation.tryBuild(SurvivorAuth.MODID, id);
        if (key == null) return null;
        return BuiltInRegistries.ITEM.getOptional(key)
                .filter(i -> i instanceof ArmorItem)
                .map(i -> (ArmorItem) i)
                .orElse(null);
    }

    /** Mostra as peças na aba "Combate" do criativo (útil para testar). */
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ITEMS.getEntries().forEach(entry -> event.accept(entry.get()));
        }
    }

    private ModItems() {}
}
