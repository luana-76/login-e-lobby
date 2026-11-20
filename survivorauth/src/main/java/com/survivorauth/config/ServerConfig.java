package com.survivorauth.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Configuração do servidor: world/serverconfig/survivorauth-server.toml */
public final class ServerConfig {
    public enum RequireLogin { OFFLINE_ONLY, ALWAYS }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<RequireLogin> REQUIRE_LOGIN;
    public static final ModConfigSpec.IntValue LOGIN_TIMEOUT_SECONDS;
    public static final ModConfigSpec.IntValue MIN_PASSWORD_LENGTH;
    public static final ModConfigSpec.IntValue HASH_ITERATIONS;
    public static final ModConfigSpec.IntValue MAX_ATTEMPTS;
    public static final ModConfigSpec.IntValue LOCKOUT_MINUTES;
    public static final ModConfigSpec.BooleanValue SHOP_GRANTS_ENABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        REQUIRE_LOGIN = b
                .comment("OFFLINE_ONLY: pede senha só quando o servidor está com online-mode=false.",
                        "ALWAYS: pede senha em qualquer servidor.")
                .defineEnum("requireLogin", RequireLogin.OFFLINE_ONLY);

        LOGIN_TIMEOUT_SECONDS = b
                .comment("Segundos que o jogador tem para entrar/registrar antes de ser expulso.")
                .defineInRange("loginTimeoutSeconds", 90, 10, 600);

        MIN_PASSWORD_LENGTH = b
                .comment("Tamanho mínimo da senha.")
                .defineInRange("minPasswordLength", 6, 1, 64);

        HASH_ITERATIONS = b
                .comment("Iterações do PBKDF2 para NOVAS senhas (mais = mais seguro e mais lento).")
                .defineInRange("hashIterations", 210_000, 10_000, 2_000_000);

        MAX_ATTEMPTS = b
                .comment("Senhas erradas permitidas por IP antes de expulsar.")
                .defineInRange("failedAttempts.maxAttempts", 5, 1, 50);

        LOCKOUT_MINUTES = b
                .comment("Minutos que o IP fica impedido de entrar depois de estourar as tentativas.",
                        "0 = só expulsa, sem bloquear.")
                .defineInRange("failedAttempts.lockoutMinutes", 10, 0, 1440);

        SHOP_GRANTS_ENABLED = b
                .comment("true: quando o jogador compra uma roupa VIP na loja, o servidor entrega a peça.",
                        "ATENÇÃO: o saldo da loja ainda é só do cliente, então o servidor não confere o pagamento.",
                        "Em servidor público deixe false até existir uma carteira no servidor.")
                .define("shopGrantsEnabled", true);

        SPEC = b.build();
    }

    private ServerConfig() {}
}
