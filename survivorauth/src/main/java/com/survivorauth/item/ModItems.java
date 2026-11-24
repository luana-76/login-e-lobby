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
        armor("capacete_saqueador", "saqueador", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("colete_saqueador", "saqueador", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_saqueador", "saqueador", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_saqueador", "saqueador", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("capuz_andarilho", "andarilho", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("poncho_andarilho", "andarilho", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_andarilho", "andarilho", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_andarilho", "andarilho", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("mascara_carniceiro", "carniceiro", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("avental_carniceiro", "carniceiro", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_carniceiro", "carniceiro", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_carniceiro", "carniceiro", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("vip_elmo_ossos", "ossos", 3, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_peitoral_ossos", "ossos", 3, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_calca_ossos", "ossos", 3, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("vip_botas_ossos", "ossos", 3, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("acc_respirador", "acc_respirador", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_oculos_soldador", "acc_oculos_soldador", 0, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("acc_ombreira_sucata", "acc_ombreira_sucata", 0, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("acc_perneiras_couro", "acc_perneiras_couro", 0, ArmorItem.Type.LEGGINGS, Rarity.COMMON);
        armor("capacete_punk", "punk", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("jaqueta_punk", "punk", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_punk", "punk", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_punk", "punk", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("capacete_infectado", "infectado", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("farda_infectado", "infectado", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_infectado", "infectado", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_infectado", "infectado", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("capacete_abrigo", "abrigo", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("macacao_abrigo", "abrigo", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_abrigo", "abrigo", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_abrigo", "abrigo", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("vip_elmo_radioativo", "radioativo", 3, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_peitoral_radioativo", "radioativo", 3, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_calca_radioativo", "radioativo", 3, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("vip_botas_radioativo", "radioativo", 3, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("acc_lanterna", "acc_lanterna", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_bandana_caveira", "acc_bandana_caveira", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_corrente", "acc_corrente", 0, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("acc_cinto_granadas", "acc_cinto_granadas", 0, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("chapeu_pistoleiro", "pistoleiro", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("colete_pistoleiro", "pistoleiro", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_pistoleiro", "pistoleiro", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_pistoleiro", "pistoleiro", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("capacete_ciborgue", "ciborgue", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("peitoral_ciborgue", "ciborgue", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_ciborgue", "ciborgue", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_ciborgue", "ciborgue", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("ushanka_inverno", "inverno", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("parca_inverno", "inverno", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_inverno", "inverno", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_inverno", "inverno", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("vip_elmo_inferno", "inferno", 3, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_peitoral_inferno", "inferno", 3, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_calca_inferno", "inferno", 3, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("vip_botas_inferno", "inferno", 3, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("acc_visor_ciber", "acc_visor_ciber", 0, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("acc_gola_pele", "acc_gola_pele", 0, ArmorItem.Type.CHESTPLATE, Rarity.COMMON);
        armor("acc_gorro_la", "acc_gorro_la", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_faca_perna", "acc_faca_perna", 0, ArmorItem.Type.LEGGINGS, Rarity.COMMON);
        armor("capacete_mecanico", "mecanico", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("macacao_mecanico", "mecanico", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_mecanico", "mecanico", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_mecanico", "mecanico", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("mascara_peste", "peste", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("casaco_peste", "peste", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_peste", "peste", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_peste", "peste", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("capacete_astronauta", "astronauta", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("traje_astronauta", "astronauta", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_astronauta", "astronauta", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_astronauta", "astronauta", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("vip_kabuto_ronin", "ronin", 3, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_peitoral_ronin", "ronin", 3, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_calca_ronin", "ronin", 3, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("vip_botas_ronin", "ronin", 3, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("acc_katana_costas", "acc_katana_costas", 0, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("acc_chapeu_palha", "acc_chapeu_palha", 0, ArmorItem.Type.HELMET, Rarity.COMMON);
        armor("acc_capa_chuva", "acc_capa_chuva", 0, ArmorItem.Type.CHESTPLATE, Rarity.COMMON);
        armor("acc_galochas", "acc_galochas", 0, ArmorItem.Type.BOOTS, Rarity.COMMON);
        armor("capacete_bombeiro", "bombeiro", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("casaco_bombeiro", "bombeiro", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_bombeiro", "bombeiro", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_bombeiro", "bombeiro", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("capacete_mineiro", "mineiro", 1, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("camisa_mineiro", "mineiro", 1, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("calca_mineiro", "mineiro", 1, ArmorItem.Type.LEGGINGS, Rarity.RARE);
        armor("botas_mineiro", "mineiro", 1, ArmorItem.Type.BOOTS, Rarity.RARE);
        armor("capuz_ninja", "ninja", 2, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("traje_ninja", "ninja", 2, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("calca_ninja", "ninja", 2, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("botas_ninja", "ninja", 2, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("vip_tricornio_pirata", "pirata", 3, ArmorItem.Type.HELMET, Rarity.EPIC);
        armor("vip_casaco_pirata", "pirata", 3, ArmorItem.Type.CHESTPLATE, Rarity.EPIC);
        armor("vip_calca_pirata", "pirata", 3, ArmorItem.Type.LEGGINGS, Rarity.EPIC);
        armor("vip_botas_pirata", "pirata", 3, ArmorItem.Type.BOOTS, Rarity.EPIC);
        armor("acc_mascara_oni", "acc_mascara_oni", 0, ArmorItem.Type.HELMET, Rarity.RARE);
        armor("acc_pala_costas", "acc_pala_costas", 0, ArmorItem.Type.CHESTPLATE, Rarity.COMMON);
        armor("acc_tanque_oxigenio", "acc_tanque_oxigenio", 0, ArmorItem.Type.CHESTPLATE, Rarity.RARE);
        armor("acc_bandagem_pernas", "acc_bandagem_pernas", 0, ArmorItem.Type.LEGGINGS, Rarity.COMMON);
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
