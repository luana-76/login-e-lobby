package com.survivorauth.client;

import java.util.List;

import com.survivorauth.client.ShopData.Item;
import com.survivorauth.client.ShopData.Kind;
import com.survivorauth.client.ShopData.Rarity;

/**
 * Roupas (categoria 6) e acessórios (categoria 7) da loja. Cada id precisa ter um item de mesmo nome em ModItems.
 * Pode editar preços e raridades à mão. Ids "vip_" ganham o selo dourado VIP.
 * Preço em cash quando premium = true, senão em dinheiro do jogo.
 */
final class ShopCatalog {
    static final List<Item> ITEMS = List.of(
            new Item("vip_mascara_caveira", "Máscara Caveira VIP", "Máscara", Rarity.LEGENDARY, Kind.OUTFIT, 6, -1, 180L, true, true, 0xFFEAE6D8),
            new Item("vip_manto_sombrio", "Manto Sombrio VIP", "Manto", Rarity.LEGENDARY, Kind.OUTFIT, 6, -1, 220L, true, true, 0xFFB02424),
            new Item("vip_capacete_artico", "Capacete Ártico VIP", "Capacete", Rarity.EPIC, Kind.OUTFIT, 6, -1, 120L, true, false, 0xFFECEEF0),
            new Item("vip_colete_artico", "Colete Ártico VIP", "Colete", Rarity.EPIC, Kind.OUTFIT, 6, -1, 150L, true, true, 0xFFECEEF0),
            new Item("vip_calca_artica", "Calça Ártica VIP", "Calça", Rarity.RARE, Kind.OUTFIT, 6, -1, 90L, true, false, 0xFFECEEF0),
            new Item("capuz_cacador", "Capuz de Caçador", "Capuz", Rarity.RARE, Kind.OUTFIT, 6, -1, 150000L, false, false, 0xFF4A5C34),
            new Item("jaqueta_cacador", "Jaqueta de Caçador", "Jaqueta", Rarity.RARE, Kind.OUTFIT, 6, -1, 300000L, false, false, 0xFF4A5C34),
            new Item("calca_cacador", "Calça de Caçador", "Calça", Rarity.RARE, Kind.OUTFIT, 6, -1, 220000L, false, false, 0xFF4A5C34),
            new Item("botas_cacador", "Botas de Caçador", "Botas", Rarity.RARE, Kind.OUTFIT, 6, -1, 120000L, false, false, 0xFF54381F),
            new Item("capacete_operador", "Capacete Operador", "Capacete", Rarity.RARE, Kind.OUTFIT, 6, -1, 180000L, false, false, 0xFFC4AC7E),
            new Item("colete_operador", "Colete Operador", "Colete", Rarity.RARE, Kind.OUTFIT, 6, -1, 320000L, false, false, 0xFFC4AC7E),
            new Item("calca_operador", "Calça Operador", "Calça", Rarity.RARE, Kind.OUTFIT, 6, -1, 230000L, false, false, 0xFFC4AC7E),
            new Item("botas_operador", "Botas Operador", "Botas", Rarity.RARE, Kind.OUTFIT, 6, -1, 130000L, false, false, 0xFF5C4630),
            new Item("mascara_hazmat", "Máscara Hazmat", "Máscara", Rarity.EPIC, Kind.OUTFIT, 6, -1, 60L, true, false, 0xFFE8C824),
            new Item("traje_hazmat", "Traje Hazmat", "Traje", Rarity.EPIC, Kind.OUTFIT, 6, -1, 90L, true, true, 0xFFE8C824),
            new Item("calca_hazmat", "Calça Hazmat", "Calça", Rarity.EPIC, Kind.OUTFIT, 6, -1, 70L, true, false, 0xFFE8C824),
            new Item("botas_hazmat", "Botas Hazmat", "Botas", Rarity.EPIC, Kind.OUTFIT, 6, -1, 45L, true, false, 0xFF24242A),
            new Item("capacete_motoqueiro", "Capacete Motoqueiro", "Capacete", Rarity.EPIC, Kind.OUTFIT, 6, -1, 65L, true, false, 0xFFBE1E1E),
            new Item("jaqueta_motoqueiro", "Jaqueta Motoqueiro", "Jaqueta", Rarity.EPIC, Kind.OUTFIT, 6, -1, 95L, true, true, 0xFFBE1E1E),
            new Item("calca_motoqueiro", "Calça Motoqueiro", "Calça", Rarity.EPIC, Kind.OUTFIT, 6, -1, 70L, true, false, 0xFF1C1C20),
            new Item("botas_motoqueiro", "Botas Motoqueiro", "Botas", Rarity.EPIC, Kind.OUTFIT, 6, -1, 45L, true, false, 0xFF1C1C20),
            new Item("vip_elmo_real", "Elmo Real VIP", "Elmo", Rarity.LEGENDARY, Kind.OUTFIT, 6, -1, 200L, true, true, 0xFFECB834),
            new Item("vip_casaco_real", "Casaco Real VIP", "Casaco", Rarity.LEGENDARY, Kind.OUTFIT, 6, -1, 260L, true, true, 0xFFECB834),
            new Item("vip_calca_real", "Calça Real VIP", "Calça", Rarity.LEGENDARY, Kind.OUTFIT, 6, -1, 190L, true, false, 0xFFECB834),
            new Item("vip_botas_reais", "Botas Reais VIP", "Botas", Rarity.LEGENDARY, Kind.OUTFIT, 6, -1, 150L, true, false, 0xFFECB834),
            new Item("acc_oculos_aviador", "Óculos Aviador", "Óculos", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 40000L, false, false, 0xFFC8A068),
            new Item("acc_oculos_noturno", "Óculos de Visão Noturna", "Óculos", Rarity.RARE, Kind.ACCESSORY, 7, -1, 350000L, false, false, 0xFF3EE86A),
            new Item("acc_bandana", "Bandana Vermelha", "Bandana", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 30000L, false, false, 0xFFB01E1E),
            new Item("acc_bone_tatico", "Boné Tático", "Boné", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 60000L, false, false, 0xFF50543C),
            new Item("acc_chapeu_cowboy", "Chapéu de Vaqueiro", "Chapéu", Rarity.RARE, Kind.ACCESSORY, 7, -1, 200000L, false, false, 0xFF78502C),
            new Item("acc_mascara_gas", "Máscara de Gás", "Máscara", Rarity.RARE, Kind.ACCESSORY, 7, -1, 400000L, false, false, 0xFF4A5238),
            new Item("acc_headset", "Headset Tático", "Headset", Rarity.RARE, Kind.ACCESSORY, 7, -1, 250000L, false, false, 0xFF5A5E66),
            new Item("vip_balaclava", "Balaclava Fantasma VIP", "Balaclava", Rarity.EPIC, Kind.ACCESSORY, 7, -1, 70L, true, true, 0xFF101014),
            new Item("vip_coroa_dourada", "Coroa de Ouro VIP", "Coroa", Rarity.LEGENDARY, Kind.ACCESSORY, 7, -1, 160L, true, true, 0xFFECB834),
            new Item("acc_tapa_olho", "Tapa-olho Pirata", "Tapa-olho", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 35000L, false, false, 0xFF101014),
            new Item("acc_cachecol", "Cachecol Vermelho", "Cachecol", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 45000L, false, false, 0xFFB01E28),
            new Item("acc_mochila", "Mochila de Sobrevivente", "Mochila", Rarity.RARE, Kind.ACCESSORY, 7, -1, 500000L, false, true, 0xFF4A523A),
            new Item("acc_luvas", "Luvas Táticas", "Luvas", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 80000L, false, false, 0xFF24221F),
            new Item("vip_bandoleira", "Bandoleira de Munição VIP", "Bandoleira", Rarity.EPIC, Kind.ACCESSORY, 7, -1, 80L, true, false, 0xFF6E4C2C),
            new Item("acc_cinto", "Cinto de Utilidades", "Cinto", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 90000L, false, false, 0xFF5C4028),
            new Item("acc_coldre", "Coldre Duplo", "Coldre", Rarity.RARE, Kind.ACCESSORY, 7, -1, 180000L, false, false, 0xFF3A322C),
            new Item("vip_joelheiras", "Joelheiras Táticas VIP", "Joelheiras", Rarity.EPIC, Kind.ACCESSORY, 7, -1, 60L, true, false, 0xFF5A5E68),
            new Item("acc_botas_combate", "Botas de Combate", "Botas", Rarity.COMMON, Kind.ACCESSORY, 7, -1, 110000L, false, false, 0xFF1C1A18),
            new Item("acc_caneleiras", "Caneleiras Táticas", "Caneleiras", Rarity.RARE, Kind.ACCESSORY, 7, -1, 160000L, false, false, 0xFF7C828E),
            new Item("vip_tenis_neon", "Tênis Neon VIP", "Tênis", Rarity.EPIC, Kind.ACCESSORY, 7, -1, 55L, true, true, 0xFF3CFF78));

    private ShopCatalog() {}
}
