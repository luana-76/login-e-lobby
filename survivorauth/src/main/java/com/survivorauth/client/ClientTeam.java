package com.survivorauth.client;

import com.survivorauth.net.Team;

/** Time do jogador neste cliente (preenchido pelo servidor quando o login termina). Só cliente. */
final class ClientTeam {
    private static Team current;

    static Team get() {
        return current;
    }

    static void set(Team team) {
        current = team;
    }

    private ClientTeam() {}
}
