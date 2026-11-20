package com.survivorauth.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.survivorauth.config.ServerConfig;

/** Controle de senhas erradas por IP (em memória; zera ao reiniciar o servidor). */
public final class LoginAttempts {
    private static final class Entry {
        int fails;
        long lockUntil;
    }

    private static final Map<String, Entry> MAP = new ConcurrentHashMap<>();

    /** Minutos restantes de bloqueio para o IP (0 = livre). */
    public static long lockedMinutes(String ip) {
        Entry e = MAP.get(ip);
        if (e == null) return 0;
        long left = e.lockUntil - System.currentTimeMillis();
        return left > 0 ? (left + 59_999L) / 60_000L : 0;
    }

    /** Registra uma senha errada. Retorna quantas tentativas restam (0 = estourou). */
    public static int fail(String ip) {
        Entry e = MAP.computeIfAbsent(ip, k -> new Entry());
        synchronized (e) {
            e.fails++;
            int max = ServerConfig.MAX_ATTEMPTS.get();
            if (e.fails >= max) {
                e.fails = 0;
                int lockout = ServerConfig.LOCKOUT_MINUTES.get();
                if (lockout > 0) e.lockUntil = System.currentTimeMillis() + lockout * 60_000L;
                return 0;
            }
            return max - e.fails;
        }
    }

    public static void reset(String ip) {
        MAP.remove(ip);
    }

    public static void clear() {
        MAP.clear();
    }

    private LoginAttempts() {}
}
