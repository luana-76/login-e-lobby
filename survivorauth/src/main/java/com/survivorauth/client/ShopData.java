package com.survivorauth.client;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import com.survivorauth.item.ModItems;
import com.survivorauth.net.ShopClaimPayload;

import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Dados da loja (só cliente). Hoje tudo é local, para a tela funcionar sozinha.
 * Para ligar ao servidor: preencha money/cash/OWNED a partir de um payload e troque o corpo de buy()
 * por um pacote de "comprar item" (o servidor deve validar o saldo, nunca confie no cliente).
 */
final class ShopData {
    enum Rarity {
        COMMON(0xFFB8B8B8), RARE(0xFF4DA3FF), EPIC(0xFFB35CFF), LEGENDARY(0xFFFFA630);

        final int color;

        Rarity(int color) {
            this.color = color;
        }

        String key() {
            return "survivorauth.shop.rarity." + name().toLowerCase(Locale.ROOT);
        }
    }

    enum Kind {
        SKIN, WEAPON, ARMOR, MISC, CRATE, OUTFIT, ACCESSORY;

        String key() {
            return "survivorauth.shop.kind." + name().toLowerCase(Locale.ROOT);
        }

        String descKey() {
            return "survivorauth.shop.desc." + name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * @param category 1 armas, 2 proteção, 3 variados, 4 personalização, 5 caixas, 6 roupas, 7 acessórios
     * @param cls      classe do item (0 fuzileiro, 1 médico, 2 engenheiro, 3 sniper) ou -1 para todas
     * @param premium  true = custa cash; false = custa dinheiro do jogo
     * @param tint     cor do desenho provisório (quando não há textura)
     */
    record Item(String id, String name, String base, Rarity rarity, Kind kind, int category, int cls,
                long price, boolean premium, boolean featured, int tint) {
        /** Roupas VIP têm id começando com "vip_" (e um item de mesmo id em ModItems). */
        boolean vip() {
            return id.startsWith("vip_");
        }

        /** true se existe uma roupa de verdade no jogo com este id (o servidor entrega ao comprar). */
        boolean wearable() {
            return ModItems.byShopId(id) != null;
        }
    }

    static long money = 99_311_235L;
    static long cash = 4_288L;
    static String vip = "MVP+";
    /** Bônus mostrados no rodapé da loja (%). */
    static final int[] BOOSTS = {30, 60, 60};

    /** Ação do botão "COMPRAR CASH" (abrir link, outra tela...). */
    static Runnable buyCashAction = () -> {};

    static final Set<String> OWNED = new HashSet<>(Set.of("ak47_sakura", "m4a1_dourada", "m4a1_temumu"));

    private static final List<Item> BASE = List.of(
            new Item("ak47_sakura", "AK-47 Sakura", "AK-47", Rarity.EPIC, Kind.SKIN, 4, 0, 0, false, false, 0xFFC8506E),
            new Item("m4a1_dourada", "M4A1 Dourada", "M4A1", Rarity.EPIC, Kind.SKIN, 4, 0, 0, false, false, 0xFFD4A020),
            new Item("m4a1_temumu", "M4A1 Temumu", "M4A1", Rarity.LEGENDARY, Kind.SKIN, 4, 0, 0, false, true, 0xFF3C8FD0),
            new Item("awp_glacial", "AWP Glacial", "AWP", Rarity.LEGENDARY, Kind.SKIN, 4, 3, 120, true, true, 0xFF7FD6E8),
            new Item("ak47", "AK-47", "AK-47", Rarity.COMMON, Kind.WEAPON, 1, 0, 450_000, false, false, 0xFF6A7074),
            new Item("m4a1", "M4A1", "M4A1", Rarity.RARE, Kind.WEAPON, 1, 0, 480_000, false, false, 0xFF6A7074),
            new Item("awp", "AWP", "AWP", Rarity.EPIC, Kind.WEAPON, 1, 3, 900_000, false, false, 0xFF6A7074),
            new Item("colete3", "Colete Nível III", "Colete", Rarity.RARE, Kind.ARMOR, 2, -1, 250_000, false, true, 0xFF5B7A52),
            new Item("capacete", "Capacete Tático", "Capacete", Rarity.COMMON, Kind.ARMOR, 2, -1, 180_000, false, false, 0xFF5B7A52),
            new Item("maleta", "Maleta Médica", "Médico", Rarity.RARE, Kind.MISC, 3, 1, 80_000, false, false, 0xFFD04040),
            new Item("kit_reparo", "Kit de Reparo", "Engenheiro", Rarity.COMMON, Kind.MISC, 3, 2, 60_000, false, false, 0xFFD0902A),
            new Item("caixa_comum", "Caixa Comum", "Caixa", Rarity.COMMON, Kind.CRATE, 5, -1, 25_000, false, false, 0xFF8A6A3A),
            new Item("caixa_lendaria", "Caixa Lendária", "Caixa", Rarity.LEGENDARY, Kind.CRATE, 5, -1, 50, true, true, 0xFFFFA630));

    /** Base + roupas e acessórios (ShopCatalog). */
    static final List<Item> ITEMS = Stream.concat(BASE.stream(), ShopCatalog.ITEMS.stream()).toList();

    static boolean owns(Item it) {
        return OWNED.contains(it.id());
    }

    static boolean canAfford(Item it) {
        return (it.premium() ? cash : money) >= it.price();
    }

    /** Compra local. Substitua por um pacote para o servidor. */
    static boolean buy(Item it) {
        if (owns(it) || !canAfford(it)) return false;
        if (it.premium()) cash -= it.price();
        else money -= it.price();
        if (it.kind() != Kind.CRATE) OWNED.add(it.id());
        claim(it);
        return true;
    }

    /**
     * Pede ao servidor para entregar/vestir a roupa (só roupas vestíveis). Também é usado pelo botão
     * "RECEBER" quando a peça já é da conta mas foi perdida (morte, drop...). Não cobra nada.
     */
    static void claim(Item it) {
        if (!it.wearable()) return;
        try {
            PacketDistributor.sendToServer(new ShopClaimPayload(it.id()));
        } catch (Exception ignored) {
            // servidor sem o mod / sem conexão: a compra local continua valendo
        }
    }

    private ShopData() {}
}
