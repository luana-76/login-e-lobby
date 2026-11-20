package com.survivorauth.client;

import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import com.survivorauth.SurvivorAuth;
import com.survivorauth.config.ClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Menu inicial mostrado logo depois do login (estilo lobby de servidor de sobrevivência).
 *
 * Personalização:
 *  - Imagens dos cartões (resource pack), todas opcionais:
 *      assets/survivorauth/textures/gui/card_server.png
 *      assets/survivorauth/textures/gui/card_pvp.png
 *      assets/survivorauth/textures/gui/card_featured.png
 *      assets/survivorauth/textures/gui/card_season.png
 *  - Fundo: o mesmo background.png da tela de login.
 *  - Textos: chaves survivorauth.menu.* em lang/pt_br.json.
 */
public class MainMenuScreen extends Screen {
    private static final String[] TAB_KEYS = {
            "home", "shop", "battlepass", "inventory", "stats", "career", "clans", "services"
    };
    private static final String[] CARD_IMAGES = {"card_server", "card_pvp", "card_featured", "card_season"};

    private static final int SHOP_TAB = 1;

    private int tab = 0;
    private final ShopPanel shop;

    // tema
    private int accent, textColor, dim;
    private float darkness;
    private boolean vignette;
    private Scenery.Tex bgTex;
    private final Scenery.Tex[] cardTex = new Scenery.Tex[4];

    // layout
    private static final int MARGIN = 28;
    private static final int BAR_H = 28;
    private int gw, contentTop, contentBottom;
    private final int[][] tabRect = new int[TAB_KEYS.length][4];   // x, y, w, h
    private final int[][] cardRect = new int[4][4];
    private final int[] playRect = new int[4];
    private int accountX;

    public MainMenuScreen() {
        super(Component.translatable("survivorauth.title"));
        accent = Scenery.parseColor(ClientConfig.ACCENT_COLOR.get(), 0xFFA3321F);
        textColor = Scenery.parseColor(ClientConfig.TEXT_COLOR.get(), 0xFFC9C5B8);
        dim = Scenery.parseColor(ClientConfig.DIM_COLOR.get(), 0xFF7A7D70);
        shop = new ShopPanel(accent, textColor, dim);
        darkness = ClientConfig.BACKGROUND_DARKNESS.get().floatValue();
        vignette = ClientConfig.VIGNETTE.get();
        bgTex = Scenery.probe(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "textures/gui/background.png"));
        for (int i = 0; i < cardTex.length; i++) {
            cardTex[i] = Scenery.probe(
                    ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "textures/gui/" + CARD_IMAGES[i] + ".png"));
        }
    }

    // ------------------------------------------------------------------ layout

    @Override
    protected void init() {
        Font font = Minecraft.getInstance().font;

        // abas: o espaço entre elas encolhe se a janela for estreita
        int total = 0;
        for (String key : TAB_KEYS) total += font.width(tabLabel(key));
        int rightReserve = 150;
        int gap = Math.max(6, Math.min(14, (width - 2 * MARGIN - rightReserve - total) / (TAB_KEYS.length - 1)));
        int x = MARGIN;
        for (int i = 0; i < TAB_KEYS.length; i++) {
            int w = font.width(tabLabel(TAB_KEYS[i]));
            tabRect[i][0] = x;
            tabRect[i][1] = 10;
            tabRect[i][2] = w;
            tabRect[i][3] = 16;
            x += w + gap;
        }
        accountX = width - MARGIN;

        // grade de cartões
        gw = Math.min(340, (int) (width * 0.44f));
        contentTop = 40;
        contentBottom = height - BAR_H - 10;
        int avail = contentBottom - contentTop;
        int g4 = 4;
        int h1 = avail * 62 / 100;
        int h2 = avail - h1 - g4;
        int cw = (gw - g4) / 2;
        int hb = (h1 - g4) / 2;

        setRect(cardRect[0], MARGIN, contentTop, cw, h1);
        setRect(cardRect[1], MARGIN + cw + g4, contentTop, gw - cw - g4, hb);
        setRect(cardRect[2], MARGIN + cw + g4, contentTop + hb + g4, gw - cw - g4, h1 - hb - g4);
        setRect(cardRect[3], MARGIN, contentTop + h1 + g4, gw, h2);

        setRect(playRect, width - 128, height - BAR_H, 128, BAR_H);
        shop.layout(font, width, MARGIN, contentTop, contentBottom);
    }

    private static void setRect(int[] r, int x, int y, int w, int h) {
        r[0] = x; r[1] = y; r[2] = w; r[3] = h;
    }

    private static boolean inside(int[] r, double mx, double my) {
        return mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
    }

    private static Component tabLabel(String key) {
        return Component.translatable("survivorauth.menu.tab." + key);
    }

    // ------------------------------------------------------------------ comportamento

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = 0; i < tabRect.length; i++) {
                int[] r = tabRect[i];
                if (mx >= r[0] - 4 && mx < r[0] + r[2] + 4 && my >= r[1] && my < r[1] + r[3]) {
                    tab = i;
                    return true;
                }
            }
            if (tab == SHOP_TAB && shop.mouseClicked(mx, my, button)) return true;
            if (tab == 0 && inside(cardRect[0], mx, my)) {
                onClose();
                return true;
            }
            if (inside(playRect, mx, my)) {
                if (tab == SHOP_TAB) shop.buyCash();
                else onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (tab == SHOP_TAB && shop.mouseScrolled(mx, my, scrollY)) return true;
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    // ------------------------------------------------------------------ desenho

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // o fundo é desenhado em render()
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        drawBackground(g);
        drawTopBar(g, font, mouseX, mouseY);

        if (tab == 0) {
            drawCards(g, font, mouseX, mouseY);
        } else if (tab == SHOP_TAB) {
            shop.render(g, font, mouseX, mouseY);
        } else {
            drawComingSoon(g, font);
        }

        drawPlayer(g, mc, mouseX, mouseY);
        drawBottomBar(g, font, mc, mouseX, mouseY);
    }

    private void drawBackground(GuiGraphics g) {
        g.fill(0, 0, width, height, 0xFF080907);
        if (bgTex != null) {
            Scenery.drawCover(g, bgTex, 0, 0, width, height);
            g.fill(0, 0, width, height, ((int) (Math.min(1f, Math.max(0f, darkness)) * 255f)) << 24);
        } else {
            Scenery.drawWilderness(g, width, height);
            g.fill(0, 0, width, height, 0x55000000);
        }
        if (vignette) {
            int steps = 24;
            int sx = Math.max(1, width / 90);
            int sy = Math.max(1, height / 60);
            for (int i = 0; i < steps; i++) {
                int a = (int) (110 * Math.pow(1.0 - i / (double) steps, 2));
                int c = a << 24;
                g.fill(i * sx, 0, (i + 1) * sx, height, c);
                g.fill(width - (i + 1) * sx, 0, width - i * sx, height, c);
                g.fill(0, i * sy, width, (i + 1) * sy, c);
                g.fill(0, height - (i + 1) * sy, width, height - i * sy, c);
            }
        }
    }

    private void drawTopBar(GuiGraphics g, Font font, int mx, int my) {
        g.fillGradient(0, 0, width, 38, 0xB0000000, 0x00000000);

        for (int i = 0; i < TAB_KEYS.length; i++) {
            int[] r = tabRect[i];
            boolean active = i == tab;
            boolean hover = mx >= r[0] - 4 && mx < r[0] + r[2] + 4 && my >= r[1] && my < r[1] + r[3];
            int color = active ? accent : hover ? textColor : Scenery.withAlpha(textColor, 0xB0);
            g.drawString(font, tabLabel(TAB_KEYS[i]), r[0], r[1] + 4, color, false);
            if (active) g.fill(r[0], r[1] + 15, r[0] + r[2], r[1] + 16, accent);
        }

        // conta (direita): rótulo pequeno + nome do jogador + menu
        int right = accountX - 22;
        String name = Minecraft.getInstance().getUser().getName();
        Component rank = Component.translatable("survivorauth.menu.rank");
        g.drawString(font, rank, right - font.width(rank), 8, accent, false);
        g.drawString(font, name, right - font.width(name), 19, 0xFFF2EEE4, false);
        for (int i = 0; i < 3; i++) g.fill(accountX - 14, 12 + i * 5, accountX, 14 + i * 5, 0xFFF2EEE4);
    }

    private void drawCards(GuiGraphics g, Font font, int mx, int my) {
        Minecraft mc = Minecraft.getInstance();
        int online = mc.getConnection() != null ? mc.getConnection().getOnlinePlayers().size() : 1;

        drawCard(g, font, 0, 0xFF2A3A38, 0xFF0F1614, mx, my,
                Component.translatable("survivorauth.menu.online", online), false, true, true);
        drawCard(g, font, 1, 0xFF3A3326, 0xFF14110C, mx, my,
                Component.translatable("survivorauth.menu.online.short"), false, true, false);
        drawCard(g, font, 2, 0xFF6A4310, 0xFF1D1206, mx, my,
                Component.translatable("survivorauth.menu.card3.tag"), true, false, false);
        drawCard(g, font, 3, 0xFF2B2F28, 0xFF0C0E0B, mx, my, Component.empty(), false, false, false);
    }

    /**
     * @param n          índice do cartão (0..3), usado para achar as chaves de texto
     * @param badge      etiqueta do canto (vazia = sem etiqueta)
     * @param accentTag  etiqueta com fundo de destaque (direita) em vez de fundo escuro (esquerda)
     * @param dot        ponto verde de "online" antes da etiqueta
     * @param watermark  escreve o título do mod grande no fundo (cartão principal)
     */
    private void drawCard(GuiGraphics g, Font font, int n, int topColor, int bottomColor, int mx, int my,
                          Component badge, boolean accentTag, boolean dot, boolean watermark) {
        int[] r = cardRect[n];
        int x = r[0], y = r[1], w = r[2], h = r[3];
        boolean hover = inside(r, mx, my);

        g.fillGradient(x, y, x + w, y + h, topColor, bottomColor);
        if (cardTex[n] != null) Scenery.drawCover(g, cardTex[n], x, y, w, h);

        if (watermark && cardTex[n] == null) {
            Component title = Component.translatable("survivorauth.title");
            float s = Math.max(1f, Math.min(2.2f, (w - 16) / (float) Math.max(1, font.width(title))));
            g.pose().pushPose();
            g.pose().translate((float) (x + w / 2), (float) (y + h * 0.30f), 0f);
            g.pose().scale(s, s, 1f);
            g.drawString(font, title, -font.width(title) / 2, -4, Scenery.withAlpha(0xFFFFFF, 0xC0), false);
            g.pose().popPose();
        }

        // sombra para o texto ficar legível sobre qualquer imagem
        g.fillGradient(x, y + h * 45 / 100, x + w, y + h, 0x00000000, 0xD0000000);

        // etiqueta
        if (!badge.getString().isEmpty()) {
            int bw = font.width(badge) + (dot ? 16 : 10);
            int bx = accentTag ? x + w - 6 - bw : x + 6;
            g.fill(bx, y + 6, bx + bw, y + 17, accentTag ? accent : 0xCC000000);
            int tx = bx + 5;
            if (dot) {
                g.fill(bx + 5, y + 10, bx + 9, y + 14, 0xFF4FD05F);
                tx = bx + 12;
            }
            g.drawString(font, badge, tx, y + 8, 0xFFF2EEE4, false);
        }

        // textos de baixo: categoria (destaque), título e subtítulo
        String p = "survivorauth.menu.card" + (n + 1);
        Component cat = Component.translatable(p + ".cat");
        Component title = Component.translatable(p + ".title");
        Component sub = Component.translatable(p + ".sub");
        boolean hasSub = !sub.getString().isEmpty();

        int ty = y + h - (hasSub ? 12 : 0) - 12 - 12 - 4;
        if (n == 3) {
            // cartão largo: texto em linhas, com quebra automática no subtítulo
            int line = y + 12;
            g.drawString(font, cat, x + 10, line, accent, false);
            g.drawString(font, title, x + 10, line + 11, 0xFFF2EEE4, false);
            int ly = line + 24;
            for (var l : font.split(sub, w - 20)) {
                if (ly > y + h - 10) break;
                g.drawString(font, l, x + 10, ly, dim, false);
                ly += 10;
            }
        } else {
            g.drawString(font, cat, x + 8, ty, accent, false);
            g.drawString(font, title, x + 8, ty + 11, 0xFFF2EEE4, false);
            if (hasSub) g.drawString(font, sub, x + 8, ty + 22, dim, false);
        }

        // moldura no hover
        if (hover) {
            g.fill(x, y, x + w, y + 1, accent);
            g.fill(x, y + h - 1, x + w, y + h, accent);
            g.fill(x, y, x + 1, y + h, accent);
            g.fill(x + w - 1, y, x + w, y + h, accent);
        }
    }

    private void drawComingSoon(GuiGraphics g, Font font) {
        int x = MARGIN, y = contentTop, w = gw, h = contentBottom - contentTop;
        g.fill(x, y, x + w, y + h, 0x99000000);
        g.fill(x, y, x + 2, y + h, accent);

        Component name = tabLabel(TAB_KEYS[tab]);
        g.pose().pushPose();
        g.pose().translate((float) (x + 16), (float) (y + 16), 0f);
        g.pose().scale(2f, 2f, 1f);
        g.drawString(font, name, 0, 0, 0xFFF2EEE4, false);
        g.pose().popPose();

        g.drawString(font, Component.translatable("survivorauth.menu.soon"), x + 16, y + 44, accent, false);
        int ly = y + 58;
        for (var l : font.split(Component.translatable("survivorauth.menu.soon.sub"), w - 32)) {
            g.drawString(font, l, x + 16, ly, dim, false);
            ly += 10;
        }
    }

    private void drawPlayer(GuiGraphics g, Minecraft mc, int mx, int my) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        int ex1 = (int) (width * 0.60f);
        int ex2 = (int) (width * 0.90f);
        int ey1 = contentTop;
        int ey2 = contentBottom;
        int scale = Math.max(20, (int) ((ey2 - ey1) * 0.40f));

        // sombra no chão
        int cx = (ex1 + ex2) / 2;
        g.fill(cx - 38, ey2 - 6, cx + 38, ey2 - 3, 0x55000000);
        g.fill(cx - 28, ey2 - 3, cx + 28, ey2 - 1, 0x33000000);

        InventoryScreen.renderEntityInInventoryFollowsMouse(g, ex1, ey1, ex2, ey2, scale, 0.0625F, mx, my, player);
    }

    private void drawBottomBar(GuiGraphics g, Font font, Minecraft mc, int mx, int my) {
        int y = height - BAR_H;
        g.fill(0, y, width, height, 0xCC050605);
        g.fill(0, y, width, y + 1, Scenery.withAlpha(dim, 0x50));

        LocalPlayer p = mc.player;
        int hp = p == null ? 100 : Math.round(p.getHealth() / Math.max(1f, p.getMaxHealth()) * 100f);
        int food = p == null ? 100 : p.getFoodData().getFoodLevel() * 5;
        int xp = p == null ? 0 : Math.round(p.experienceProgress * 100f);

        boolean shopTab = tab == SHOP_TAB;
        int tx = MARGIN;
        if (shopTab) {
            tx = shop.drawBoosts(g, font, tx, y);
        } else {
            tx = stat(g, font, tx, y, 0xFFD04040, Component.translatable("survivorauth.menu.stat.health", hp + "%"));
            tx = stat(g, font, tx, y, 0xFFD0902A, Component.translatable("survivorauth.menu.stat.food", food + "%"));
            stat(g, font, tx, y, 0xFF4FB04F, Component.translatable("survivorauth.menu.stat.xp", xp + "%"));
        }
        int limit = shopTab ? shop.drawWallet(g, font, playRect[0] - 12, y) : playRect[0];

        // centro: jogadores online + dica do chat
        int online = mc.getConnection() != null ? mc.getConnection().getOnlinePlayers().size() : 1;
        Component mid = Component.translatable("survivorauth.menu.chat", online);
        int midX = Math.max(tx + 130, (width - font.width(mid)) / 2);
        if (midX + font.width(mid) < limit - 10) {
            g.drawString(font, mid, midX, y + 10, Scenery.withAlpha(textColor, 0xA0), false);
        }

        // botão principal
        boolean hover = inside(playRect, mx, my);
        g.fill(playRect[0], playRect[1], playRect[0] + playRect[2], playRect[1] + playRect[3],
                hover ? Scenery.lighten(accent) : accent);
        g.drawCenteredString(font, Component.translatable(shopTab ? "survivorauth.shop.buycash" : "survivorauth.menu.play"),
                playRect[0] + playRect[2] / 2, y + 10, 0xFFF2EEE4);
    }

    private int stat(GuiGraphics g, Font font, int x, int y, int color, Component text) {
        g.fill(x, y + 9, x + 9, y + 18, color);
        g.drawString(font, text, x + 14, y + 10, textColor, false);
        return x + 14 + font.width(text) + 16;
    }

    @SuppressWarnings("unused")
    private static String fmt(long seconds) {
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }
}
