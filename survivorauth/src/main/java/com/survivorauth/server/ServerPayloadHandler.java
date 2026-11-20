package com.survivorauth.server;

import com.survivorauth.net.AuthSubmitPayload;
import com.survivorauth.net.ShopClaimPayload;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerPayloadHandler {
    public static void handleSubmit(AuthSubmitPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                if (payload.register()) {
                    AuthService.register(player, payload.password(), payload.confirm());
                } else {
                    AuthService.login(player, payload.password());
                }
            }
        });
    }

    public static void handleShopClaim(ShopClaimPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player && !AuthService.isPending(player)) {
                ShopRewards.grant(player, payload.id());
            }
        });
    }

    private ServerPayloadHandler() {}
}
