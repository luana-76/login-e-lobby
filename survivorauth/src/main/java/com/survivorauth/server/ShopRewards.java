package com.survivorauth.server;

import com.survivorauth.config.ServerConfig;
import com.survivorauth.item.ModItems;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Entrega no servidor as roupas compradas na loja.
 *
 * ATENÇÃO: o saldo da loja ainda é local (ShopData, só cliente), então o servidor não consegue
 * conferir se o jogador pagou. Enquanto não existir uma carteira no servidor, deixe
 * "shopGrantsEnabled" desligado em servidores públicos e dê as peças por comando/crate.
 */
public final class ShopRewards {

    public static void grant(ServerPlayer player, String shopId) {
        if (!ServerConfig.SHOP_GRANTS_ENABLED.get()) return;

        ArmorItem item = ModItems.byShopId(shopId);
        if (item == null) return;                 // só roupas do mod; qualquer outro id é ignorado
        if (carrying(player, item)) return;       // já tem: não duplica

        ItemStack stack = new ItemStack(item);
        EquipmentSlot slot = item.getEquipmentSlot();
        if (player.getItemBySlot(slot).isEmpty()) {
            player.setItemSlot(slot, stack);      // já aparece no boneco
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.displayClientMessage(Component.translatable("survivorauth.shop.granted", item.getDescription()), true);
    }

    private static boolean carrying(ServerPlayer player, ArmorItem item) {
        var inv = player.getInventory();
        for (ItemStack s : inv.items) if (s.is(item)) return true;
        for (ItemStack s : inv.armor) if (s.is(item)) return true;
        for (ItemStack s : inv.offhand) if (s.is(item)) return true;
        return false;
    }

    private ShopRewards() {}
}
