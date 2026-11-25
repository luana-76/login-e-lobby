package com.survivorauth.client;

/** Contagens recebidas do servidor. */
final class ClientTeamCounts {
    private static final int[] COUNTS = new int[4];
    static void set(int leste, int oeste, int norte, int sul) {
        COUNTS[0] = Math.max(0, leste);
        COUNTS[1] = Math.max(0, oeste);
        COUNTS[2] = Math.max(0, norte);
        COUNTS[3] = Math.max(0, sul);
    }
    static int get(int index) { return COUNTS[index]; }
    private ClientTeamCounts() {}
}
