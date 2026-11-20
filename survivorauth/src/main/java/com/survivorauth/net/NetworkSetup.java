package com.survivorauth.net;

import com.survivorauth.client.ClientPayloadHandler;
import com.survivorauth.server.ServerPayloadHandler;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkSetup {
    public static void register(RegisterPayloadHandlersEvent event) {
        // optional(): jogadores SEM o mod no cliente continuam podendo entrar
        // (eles usam /register e /login no chat).
        PayloadRegistrar registrar = event.registrar("1").optional();

        // Lambda em vez de ClientPayloadHandler::handleState: assim a classe do cliente
        // só é carregada quando realmente roda no cliente, e o servidor dedicado não quebra.
        registrar.playToClient(AuthStatePayload.TYPE, AuthStatePayload.CODEC, (payload, ctx) -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPayloadHandler.handleState(payload, ctx);
            }
        });
        registrar.playToServer(AuthSubmitPayload.TYPE, AuthSubmitPayload.CODEC, ServerPayloadHandler::handleSubmit);
        registrar.playToServer(ShopClaimPayload.TYPE, ShopClaimPayload.CODEC, ServerPayloadHandler::handleShopClaim);
    }

    private NetworkSetup() {}
}