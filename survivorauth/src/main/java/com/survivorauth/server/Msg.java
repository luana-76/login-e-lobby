package com.survivorauth.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Todas as mensagens que o SERVIDOR envia (chat, kick, action bar).
 * Edite os textos aqui para mudar o tom. A tela do cliente usa os arquivos de idioma (lang).
 */
public final class Msg {
    private static Component red(String s) {
        return Component.literal("[Sobrevivente] " + s).withStyle(ChatFormatting.RED);
    }

    private static Component gray(String s) {
        return Component.literal("[Sobrevivente] " + s).withStyle(ChatFormatting.GRAY);
    }

    private static Component green(String s) {
        return Component.literal("[Sobrevivente] " + s).withStyle(ChatFormatting.GREEN);
    }

    // --- dicas para jogadores sem o mod no cliente ---
    public static Component hint(boolean registered) {
        return registered
                ? gray("Identifique-se: /login <senha>")
                : gray("Registre-se: /register <senha> <senha>");
    }

    // --- sucesso ---
    public static Component welcome(String name) {
        return green("Identidade confirmada. Boa sorte lá fora, " + name + ".");
    }

    public static Component registered() {
        return green("Registro concluído. Você está dentro.");
    }

    public static Component passwordChanged() {
        return green("Senha alterada.");
    }

    public static Component teamChosen(String name, String teamLabel) {
        return green("Bem-vindo ao time " + teamLabel + ", " + name + ". Boa sorte lá fora.");
    }

    public static Component teamHint() {
        return gray("Escolha seu time (só uma vez!): /team leste | oeste | norte | sul");
    }

    public static Component teamCurrent(String teamLabel) {
        return gray("Seu time é " + teamLabel + ". A escolha é definitiva.");
    }

    public static Component teamNone() {
        return gray("Você ainda não escolheu um time.");
    }

    public static Component teamAlready(String teamLabel) {
        return red("Você já está no time " + teamLabel + ". Não dá para trocar.");
    }

    public static Component teamInvalid() {
        return red("Time inválido. Use: leste, oeste, norte ou sul.");
    }

    public static Component teamFirst() {
        return red("Escolha seu time antes de continuar: /team leste | oeste | norte | sul");
    }

    // --- erros ---
    public static Component wrongPassword(int left) {
        return red("Senha incorreta. Tentativas restantes: " + left);
    }

    public static Component tooShort(int min) {
        return red("Senha muito curta (mínimo " + min + " caracteres).");
    }

    public static Component mismatch() {
        return red("As senhas não coincidem.");
    }

    public static Component alreadyRegistered() {
        return red("Este nome já está registrado. Use /login <senha>.");
    }

    public static Component notRegistered() {
        return red("Este nome ainda não está registrado. Use /register <senha> <senha>.");
    }

    public static Component alreadyLoggedIn() {
        return gray("Você já está identificado.");
    }

    public static Component notLoggedIn() {
        return red("Identifique-se primeiro.");
    }

    public static Component busy() {
        return gray("Aguarde, verificação em andamento...");
    }

    public static Component chatBlocked() {
        return red("Você precisa se identificar antes de falar ou usar comandos.");
    }

    public static Component wrongCurrent() {
        return red("A senha atual está incorreta.");
    }

    public static Component internalError() {
        return red("Erro interno ao verificar a senha. Avise um administrador.");
    }

    // --- kicks ---
    public static Component timeout() {
        return Component.literal("Tempo esgotado: você não se identificou a tempo.");
    }

    public static Component tooManyAttempts(int lockoutMinutes) {
        return lockoutMinutes > 0
                ? Component.literal("Muitas tentativas erradas. Tente novamente em " + lockoutMinutes + " min.")
                : Component.literal("Muitas tentativas erradas.");
    }

    public static Component lockedOut(long minutes) {
        return Component.literal("Acesso bloqueado temporariamente. Tente novamente em " + minutes + " min.");
    }

    private Msg() {}
}
