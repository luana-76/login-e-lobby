package com.survivorauth.net;

import java.util.Locale;

/** Os quatro times. A ordem aqui é a ordem dos cartões na tela de escolha. */
public enum Team {
    LESTE("leste", 0xFFE3A12F),
    OESTE("oeste", 0xFFC8473A),
    NORTE("norte", 0xFF4FA8D8),
    SUL("sul", 0xFF5DAA4B);

    public final String id;
    public final int color;

    Team(String id, int color) {
        this.id = id;
        this.color = color;
    }

    /** Devolve o time pelo id salvo na conta ("leste", ...) ou null se não existir. */
    public static Team byId(String id) {
        if (id == null) return null;
        String k = id.trim().toLowerCase(Locale.ROOT);
        for (Team t : values()) if (t.id.equals(k)) return t;
        return null;
    }

    /** Valor enviado no pacote SUCCESS: 0 = sem time, senão posição + 1. */
    public int wire() {
        return ordinal() + 1;
    }

    public static Team fromWire(int v) {
        Team[] all = values();
        return v >= 1 && v <= all.length ? all[v - 1] : null;
    }
}
