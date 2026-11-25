package com.survivorauth.net;

/** Estados que o servidor envia para a tela de login do cliente. */
public final class AuthState {
    public static final int PROMPT_LOGIN = 0;     // abrir/atualizar a tela em modo "entrar"
    public static final int PROMPT_REGISTER = 1;  // abrir/atualizar a tela em modo "registrar"
    public static final int WRONG_PASSWORD = 2;   // arg = tentativas restantes
    public static final int TOO_SHORT = 3;        // arg = tamanho mínimo
    public static final int MISMATCH = 4;         // senhas diferentes
    public static final int SUCCESS = 5;          // fechar a tela (arg = time: 0 = nenhum, senão Team.wire())
    public static final int LOCKED = 6;           // arg = minutos
    public static final int BUSY = 7;             // já existe uma verificação em andamento
    public static final int PROMPT_TEAM = 8;      // logou, mas ainda não escolheu o time: abrir a tela de times

    private AuthState() {}
}
