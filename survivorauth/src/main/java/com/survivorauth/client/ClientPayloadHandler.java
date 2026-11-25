package com.survivorauth.client;

import com.survivorauth.config.ClientConfig;
import com.survivorauth.net.AuthState;
import com.survivorauth.net.AuthStatePayload;
import com.survivorauth.net.Team;
import com.survivorauth.net.TeamCountsPayload;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Só é executado no cliente: abre/atualiza as telas de login e de escolha de time
 * conforme o servidor manda. Fluxo: LoginScreen -> (1º acesso) TeamSelectScreen -> lobby.
 */
public final class ClientPayloadHandler {
    public static void handleTeamCounts(TeamCountsPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientTeamCounts.set(payload.leste(), payload.oeste(), payload.norte(), payload.sul());
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof TeamSelectScreen screen) screen.applyCounts();
        });
    }

    public static void handleState(AuthStatePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            int state = payload.state();
            boolean authScreen = mc.screen instanceof LoginScreen || mc.screen instanceof TeamSelectScreen;

            if (state == AuthState.SUCCESS) {
                ClientTeam.set(Team.fromWire(payload.arg()));
                if (authScreen) {
                    // depois do login (e da escolha do time): lobby (ou direto no jogo, se desativado na config)
                    mc.setScreen(ClientConfig.SHOW_MENU.get() ? new MainMenuScreen() : null);
                }
                return;
            }

            if (state == AuthState.PROMPT_TEAM) {
                if (mc.screen instanceof TeamSelectScreen screen) {
                    screen.applyState(payload.seconds());
                } else {
                    mc.setScreen(new TeamSelectScreen(payload.seconds()));
                }
                return;
            }

            if (mc.screen instanceof LoginScreen screen) {
                screen.applyState(state, payload.arg(), payload.seconds());
                return;
            }
            if (mc.screen instanceof TeamSelectScreen) {
                return; // já identificado; ignora avisos atrasados de login
            }

            // tela ainda não está aberta: só abre quando o servidor pede identificação
            if (state == AuthState.PROMPT_LOGIN || state == AuthState.PROMPT_REGISTER) {
                mc.setScreen(new LoginScreen(state == AuthState.PROMPT_REGISTER, payload.seconds()));
            }
        });
    }

    private ClientPayloadHandler() {}
}
