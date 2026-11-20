package com.survivorauth.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Configuração visual da tela de login: config/survivorauth-client.toml */
public final class ClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> ACCENT_COLOR;
    public static final ModConfigSpec.ConfigValue<String> TEXT_COLOR;
    public static final ModConfigSpec.ConfigValue<String> DIM_COLOR;
    public static final ModConfigSpec.ConfigValue<String> PANEL_COLOR;
    public static final ModConfigSpec.DoubleValue BACKGROUND_DARKNESS;
    public static final ModConfigSpec.BooleanValue SCANLINES;
    public static final ModConfigSpec.BooleanValue VIGNETTE;
    public static final ModConfigSpec.BooleanValue FLICKER;
    public static final ModConfigSpec.BooleanValue SHOW_MENU;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Cores em hexadecimal (#RRGGBB).").push("colors");
        ACCENT_COLOR = b.comment("Cor de destaque (título, bordas, botão principal). Padrão: vermelho sangue seco.")
                .define("accent", "#A3321F");
        TEXT_COLOR = b.comment("Cor do texto principal.")
                .define("text", "#C9C5B8");
        DIM_COLOR = b.comment("Cor do texto secundário (rótulos, rodapé).")
                .define("dim", "#7A7D70");
        PANEL_COLOR = b.comment("Cor de fundo do painel.")
                .define("panel", "#0C0E0B");
        b.pop();

        b.comment("Efeitos.").push("effects");
        BACKGROUND_DARKNESS = b.comment("Escurecimento sobre a imagem de fundo (0 = nenhum, 1 = preto).")
                .defineInRange("backgroundDarkness", 0.55D, 0.0D, 1.0D);
        SCANLINES = b.comment("Linhas de varredura estilo monitor antigo.")
                .define("scanlines", true);
        VIGNETTE = b.comment("Escurecer as bordas da tela.")
                .define("vignette", true);
        FLICKER = b.comment("Título pulsando levemente.")
                .define("flicker", true);
        b.pop();

        b.comment("Menu inicial.").push("menu");
        SHOW_MENU = b.comment("Mostrar o menu inicial (abas, cartões, seu personagem) depois do login.")
                .define("showMenuAfterLogin", true);
        b.pop();

        SPEC = b.build();
    }

    private ClientConfig() {}
}
