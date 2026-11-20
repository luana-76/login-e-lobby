package com.survivorauth.client;

import com.survivorauth.config.ClientConfig;
import com.survivorauth.net.AuthState;
import com.survivorauth.net.AuthStatePayload;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Só é executado no cliente: abre/atualiza a tela de login conforme o servidor manda. */
public final class ClientPayloadHandler {
    public static void handleState(AuthStatePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            int state = payload.state();

            if (mc.screen instanceof LoginScreen screen) {
                if (state == AuthState.SUCCESS) {
                    // depois do login: menu inicial (ou direto no jogo, se desativado na config)
                    mc.setScreen(ClientConfig.SHOW_MENU.get() ? new MainMenuScreen() : null);
                } else {
                    screen.applyState(state, payload.arg(), payload.seconds());
                }
                return;
            }

            // tela ainda não está aberta: só abre quando o servidor pede identificação
            if (state == AuthState.PROMPT_LOGIN || state == AuthState.PROMPT_REGISTER) {
                mc.setScreen(new LoginScreen(state == AuthState.PROMPT_REGISTER, payload.seconds()));
            }
        });
    }

    private ClientPayloadHandler() {}
}
