package com.survivorauth.client;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.survivorauth.SurvivorAuth;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Painel da aba LOJA, desenhado por cima do MainMenuScreen (que mantém abas, jogador e rodapé).
 *
 * Imagens dos itens (opcionais): assets/survivorauth/textures/gui/shop/<id>.png
 * Sem imagem, o item aparece como um desenho provisório colorido.
 */
final class ShopPanel {
    private static final String[] CATS = {"best", "weapons", "armor", "misc", "custom", "crates", "outfits", "accessories"};
    private static final String[] CLASSES = {"rifleman", "medic", "engineer", "sniper"};
    private static final int COLS = 3, GAP = 4, CARD_H = 48, DETAIL_H = 122;
    private static final int GREEN = 0xFF4FD05F, BLUE = 0xFF4FA3D8, WHITE = 0xFFF2EEE4, RED = 0xFFE05050,
            GOLD = 0xFFD9A81E;
    private static final NumberFormat NUM = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR"));

    private final int accent, textColor, dim;
    private final Map<String, Scenery.Tex> tex = new HashMap<>();

    private int cat = 0, cls = 0, scroll = 0;
    private ShopData.Item selected;
    private List<ShopData.Item> visible = List.of();

    private int px, pw, classY, catY, gridTop, gridBottom, detailTop, bottom;
    private float catScale = 1f;
    private final int[][] classRect = new int[CLASSES.length][4];
    private final int[][] catRect = new int[CATS.length][4];
    private final int[] buyRect = new int[4];

    ShopPanel(int accent, int textColor, int dim) {
        this.accent = accent;
        this.textColor = textColor;
        this.dim = dim;
        for (var it : ShopData.ITEMS) {
            tex.put(it.id(), Scenery.probe(
                    ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "textures/gui/shop/" + it.id() + ".png")));
        }
    }

    // ------------------------------------------------------------------ layout

    void layout(Font font, int screenW, int left, int top, int bottom) {
        px = left;
        pw = Math.min(420, (int) (screenW * 0.50f));
        this.bottom = bottom;

        classY = top;
        for (int i = 0; i < CLASSES.length; i++) set(classRect[i], px + i * 46, classY, 44, 24);

        catY = top + 30;
        int total = 0;
        for (String c : CATS) total += font.width(catLabel(c));
        catScale = Math.min(1f, (pw - (CATS.length - 1) * 4) / (float) Math.max(1, total));
        int gap = Math.max(4, Math.min(14, (int) ((pw - total * catScale) / (CATS.length - 1))));
        int x = px;
        for (int i = 0; i < CATS.length; i++) {
            int w = Math.round(font.width(catLabel(CATS[i])) * catScale);
            set(catRect[i], x, catY, w, 14);
            x += w + gap;
        }

        gridTop = catY + 22;
        detailTop = bottom - DETAIL_H;
        gridBottom = detailTop - 6;
        set(buyRect, px + pw - 104, bottom - 22, 104, 18);
        refresh();
    }

    private void refresh() {
        List<ShopData.Item> list = new ArrayList<>();
        for (var it : ShopData.ITEMS) {
            if (it.cls() != -1 && it.cls() != cls) continue;
            if (cat == 0 ? !it.featured() : it.category() != cat) continue;
            list.add(it);
        }
        visible = list;
        if (selected == null || !visible.contains(selected)) selected = visible.isEmpty() ? null : visible.get(0);
        scroll = 0;
    }

    private int visibleRows() {
        return Math.max(1, (gridBottom - gridTop + GAP) / (CARD_H + GAP));
    }

    private int[] cardAt(int i) {
        int cw = (pw - (COLS - 1) * GAP) / COLS;
        int row = i / COLS - scroll;
        return new int[] {px + (i % COLS) * (cw + GAP), gridTop + row * (CARD_H + GAP), cw, CARD_H};
    }

    private static void set(int[] r, int x, int y, int w, int h) {
        r[0] = x; r[1] = y; r[2] = w; r[3] = h;
    }

    private static boolean in(int[] r, double mx, double my) {
        return mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
    }

    private static Component catLabel(String key) {
        return Component.translatable("survivorauth.shop.cat." + key);
    }

    private static String fmt(long v) {
        return NUM.format(v);
    }

    // ------------------------------------------------------------------ entrada

    boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return false;
        for (int i = 0; i < classRect.length; i++) {
            if (in(classRect[i], mx, my)) {
                cls = i;
                refresh();
                return true;
            }
        }
        for (int i = 0; i < catRect.length; i++) {
            int[] r = catRect[i];
            if (mx >= r[0] - 3 && mx < r[0] + r[2] + 3 && my >= r[1] && my < r[1] + r[3]) {
                cat = i;
                refresh();
                return true;
            }
        }
        if (my >= gridTop && my < gridBottom) {
            for (int i = 0; i < visible.size(); i++) {
                if (in(cardAt(i), mx, my)) {
                    selected = visible.get(i);
                    return true;
                }
            }
        }
        if (selected != null && in(buyRect, mx, my)) {
            if (ShopData.owns(selected)) ShopData.claim(selected); // roupa já comprada: pede a peça de novo
            else ShopData.buy(selected);
            return true;
        }
        return false;
    }

    boolean mouseScrolled(double mx, double my, double dy) {
        if (mx < px || mx >= px + pw || my < gridTop || my >= gridBottom) return false;
        int rows = (visible.size() + COLS - 1) / COLS;
        int max = Math.max(0, rows - visibleRows());
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(dy)));
        return true;
    }

    void buyCash() {
        ShopData.buyCashAction.run();
    }

    // ------------------------------------------------------------------ desenho

    void render(GuiGraphics g, Font font, int mx, int my) {
        drawClasses(g, font, mx, my);
        drawCategories(g, font, mx, my);
        drawGrid(g, font, mx, my);
        drawDetail(g, font, mx, my);
    }

    private void text(GuiGraphics g, Font font, Component c, float x, float y, float s, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().scale(s, s, 1f);
        g.drawString(font, c, 0, 0, color, false);
        g.pose().popPose();
    }

    private void drawClasses(GuiGraphics g, Font font, int mx, int my) {
        for (int i = 0; i < CLASSES.length; i++) {
            int[] r = classRect[i];
            int c = i == cls ? accent : in(r, mx, my) ? textColor : dim;
            int cx = r[0] + r[2] / 2;
            classIcon(g, i, cx, r[1], c);
            Component l = Component.translatable("survivorauth.shop.class." + CLASSES[i]);
            text(g, font, l, cx - font.width(l) * 0.7f / 2f, r[1] + 15, 0.7f, c);
        }
    }

    /** Ícones simples feitos com retângulos (11x11). */
    private void classIcon(GuiGraphics g, int i, int cx, int y, int c) {
        switch (i) {
            case 0 -> { // fuzil
                g.fill(cx - 6, y + 3, cx + 6, y + 5, c);
                g.fill(cx - 6, y + 3, cx - 3, y + 8, c);
                g.fill(cx - 1, y + 5, cx + 1, y + 10, c);
                g.fill(cx + 3, y + 5, cx + 4, y + 7, c);
            }
            case 1 -> { // cruz
                g.fill(cx - 2, y, cx + 3, y + 11, c);
                g.fill(cx - 5, y + 3, cx + 6, y + 8, c);
            }
            case 2 -> { // martelo
                g.fill(cx - 5, y, cx + 6, y + 4, c);
                g.fill(cx - 1, y + 4, cx + 2, y + 11, c);
            }
            default -> { // mira
                g.fill(cx - 6, y + 5, cx - 2, y + 6, c);
                g.fill(cx + 3, y + 5, cx + 7, y + 6, c);
                g.fill(cx, y, cx + 1, y + 4, c);
                g.fill(cx, y + 7, cx + 1, y + 11, c);
                g.fill(cx - 2, y + 3, cx + 3, y + 4, c);
                g.fill(cx - 2, y + 7, cx + 3, y + 8, c);
            }
        }
    }

    private void drawCategories(GuiGraphics g, Font font, int mx, int my) {
        g.fill(px, catY + 16, px + pw, catY + 17, Scenery.withAlpha(dim, 0x50));
        for (int i = 0; i < CATS.length; i++) {
            int[] r = catRect[i];
            boolean on = i == cat;
            boolean hov = mx >= r[0] - 3 && mx < r[0] + r[2] + 3 && my >= r[1] && my < r[1] + r[3];
            int c = on ? accent : hov ? textColor : Scenery.withAlpha(textColor, 0xB0);
            text(g, font, catLabel(CATS[i]), r[0], r[1] + 3 + (1f - catScale) * 3f, catScale, c);
            if (on) g.fill(r[0], catY + 16, r[0] + r[2], catY + 17, accent);
        }
    }

    private void drawGrid(GuiGraphics g, Font font, int mx, int my) {
        if (visible.isEmpty()) {
            g.drawString(font, Component.translatable("survivorauth.shop.empty"), px, gridTop + 6, dim, false);
            return;
        }
        g.enableScissor(px, gridTop, px + pw, gridBottom);
        for (int i = 0; i < visible.size(); i++) {
            int[] r = cardAt(i);
            if (r[1] + r[3] <= gridTop || r[1] >= gridBottom) continue;
            var it = visible.get(i);
            drawCard(g, font, it, r, it == selected, in(r, mx, my) && my < gridBottom);
        }
        g.disableScissor();

        int rows = (visible.size() + COLS - 1) / COLS;
        if (rows > visibleRows()) { // marcador de rolagem
            int trackH = gridBottom - gridTop;
            int barH = Math.max(10, trackH * visibleRows() / rows);
            int barY = gridTop + (trackH - barH) * scroll / Math.max(1, rows - visibleRows());
            g.fill(px + pw + 3, barY, px + pw + 4, barY + barH, Scenery.withAlpha(textColor, 0x80));
        }
    }

    private void drawCard(GuiGraphics g, Font font, ShopData.Item it, int[] r, boolean sel, boolean hov) {
        int x = r[0], y = r[1], w = r[2], h = r[3];
        g.fillGradient(x, y, x + w, y + h, 0xE0171B1C, 0xE00B0D0E);

        drawItemImage(g, it, x + 6, y + 12, w - 12, h - 24);

        Component name = Component.literal(it.name());
        float s = 0.75f;
        text(g, font, name, Math.max(x + 4, x + w - 4 - font.width(name) * s), y + 4, s, WHITE);

        if (ShopData.owns(it)) {
            text(g, font, Component.translatable("survivorauth.shop.owned"), x + 5, y + h - 9, s, GREEN);
        } else {
            drawPrice(g, font, it, x + 5, y + h - 10, s);
        }

        if (it.vip()) { // selo dourado no canto de baixo
            int tw = Math.round(font.width("VIP") * 0.7f) + 6;
            g.fill(x + w - tw - 4, y + h - 12, x + w - 4, y + h - 3, GOLD);
            text(g, font, Component.translatable("survivorauth.shop.vip"), x + w - tw - 1, y + h - 10, 0.7f, 0xFF1A1405);
        }

        int bc = sel ? accent : hov ? Scenery.withAlpha(textColor, 0x70)
                : it.vip() ? Scenery.withAlpha(GOLD, 0x90) : Scenery.withAlpha(dim, 0x40);
        border(g, x, y, w, h, bc);
        if (sel) border(g, x + 1, y + 1, w - 2, h - 2, Scenery.withAlpha(accent, 0x50));
    }

    private void drawPrice(GuiGraphics g, Font font, ShopData.Item it, int x, int y, float s) {
        String num = fmt(it.price());
        if (it.premium()) {
            coin(g, x, y + 1, 6, RED);
            text(g, font, Component.literal(num), x + 9, y, s, RED);
        } else {
            text(g, font, Component.literal("$ " + num), x, y, s, GREEN);
        }
    }

    private int priceWidth(Font font, ShopData.Item it) {
        String num = fmt(it.price());
        return it.premium() ? 9 + font.width(num) : font.width("$ " + num);
    }

    private void drawDetail(GuiGraphics g, Font font, int mx, int my) {
        g.fill(px, detailTop, px + pw, detailTop + 1, Scenery.withAlpha(dim, 0x50));
        var it = selected;
        if (it == null) return;

        int x = px, y = detailTop + 8;
        g.drawString(font, Component.literal(it.name()), x, y, accent, false);
        drawItemImage(g, it, x, y + 14, 150, 46);

        g.drawString(font, Component.translatable(it.rarity().key()), x, y + 64, it.rarity().color, false);
        Component kind = Component.empty().append(Component.translatable(it.kind().key()))
                .append(Component.literal(" \u2022 " + it.base()));
        g.drawString(font, kind, x, y + 74, textColor, false);

        int ly = y + 86;
        for (var l : font.split(Component.translatable(it.kind().descKey()), Math.max(60, pw - 112))) {
            if (ly > bottom - 8) break;
            g.drawString(font, l, x, ly, BLUE, false);
            ly += 10;
        }

        boolean owned = ShopData.owns(it);
        boolean afford = ShopData.canAfford(it);
        boolean hov = in(buyRect, mx, my);
        boolean reclaim = owned && it.wearable(); // roupa da conta: botão "RECEBER" entrega a peça de novo
        int bg = reclaim ? (hov ? Scenery.lighten(accent) : accent)
                : owned ? 0xFF2A323A : !afford ? 0xFF3A2424 : hov ? Scenery.lighten(accent) : accent;
        g.fill(buyRect[0], buyRect[1], buyRect[0] + buyRect[2], buyRect[1] + buyRect[3], bg);
        String key = reclaim ? "equip" : owned ? "owned" : !afford ? "nofunds" : "buy";
        g.drawCenteredString(font, Component.translatable("survivorauth.shop." + key),
                buyRect[0] + buyRect[2] / 2, buyRect[1] + 5, owned && !reclaim ? dim : WHITE);
        if (!owned) {
            drawPrice(g, font, it, buyRect[0] + buyRect[2] - priceWidth(font, it), buyRect[1] - 12, 1f);
        }
    }

    // ------------------------------------------------------------------ imagens

    private void drawItemImage(GuiGraphics g, ShopData.Item it, int x, int y, int w, int h) {
        Scenery.Tex t = tex.get(it.id());
        if (t != null) {
            Scenery.drawContain(g, t, x, y, w, h);
        } else {
            placeholder(g, it, x, y, w, h);
        }
    }

    /** Desenho provisório em um quadro de 60x22. */
    private void placeholder(GuiGraphics g, ShopData.Item it, int x, int y, int w, int h) {
        float s = Math.min(w / 60f, h / 22f);
        g.pose().pushPose();
        g.pose().translate(x + (w - 60 * s) / 2f, y + (h - 22 * s) / 2f, 0f);
        g.pose().scale(s, s, 1f);
        int t = it.tint();
        switch (it.kind()) {
            case SKIN, WEAPON -> {
                int body = it.kind() == ShopData.Kind.SKIN ? 0xFF23272A : 0xFF4A4F52;
                g.fill(34, 6, 60, 8, body);       // cano
                g.fill(22, 5, 40, 10, t);         // guarda-mão
                g.fill(8, 5, 24, 12, body);       // ferrolho
                g.fill(0, 6, 9, 15, t);           // coronha
                g.fill(18, 12, 23, 21, t);        // carregador
                g.fill(11, 12, 15, 19, body);     // empunhadura
                g.fill(14, 3, 24, 5, body);       // mira
            }
            case ARMOR -> {
                g.fill(14, 0, 26, 8, t);
                g.fill(34, 0, 46, 8, t);
                g.fill(18, 6, 42, 22, t);
                g.fill(18, 13, 42, 15, 0x55000000);
            }
            case CRATE -> {
                g.fill(14, 3, 46, 21, t);
                g.fill(14, 3, 46, 8, Scenery.lighten(t));
                g.fill(28, 8, 32, 13, 0xFF1A1A1A);
            }
            default -> {
                g.fill(26, 0, 34, 22, t);
                g.fill(19, 7, 41, 15, t);
            }
        }
        g.pose().popPose();
    }

    private static void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static void coin(GuiGraphics g, int x, int y, int n, int c) {
        int k = Math.max(1, n / 4);
        g.fill(x + k, y, x + n - k, y + n, c);
        g.fill(x, y + k, x + n, y + n - k, c);
        g.fill(x + 1, y + 1, x + n - 1, y + n - 1, c);
    }

    // ------------------------------------------------------------------ rodapé da loja

    /** Bônus à esquerda do rodapé. Devolve o x onde terminou. */
    int drawBoosts(GuiGraphics g, Font font, int x, int y) {
        int[] colors = {0xFF4D8DFF, 0xFFE0C030, 0xFF4FB04F};
        for (int i = 0; i < ShopData.BOOSTS.length; i++) {
            Component l = Component.translatable("survivorauth.shop.boost." + i, ShopData.BOOSTS[i] + "%");
            g.fill(x, y + 9, x + 9, y + 18, colors[i]);
            g.drawString(font, l, x + 14, y + 10, textColor, false);
            x += 14 + font.width(l) + 16;
        }
        return x;
    }

    /** Selo VIP, dinheiro e cash, alinhados à direita. Devolve o x mais à esquerda ocupado. */
    int drawWallet(GuiGraphics g, Font font, int right, int y) {
        int x = right;
        String cashS = fmt(ShopData.cash);
        x -= font.width(cashS);
        g.drawString(font, cashS, x, y + 10, WHITE, false);
        x -= 13;
        coin(g, x, y + 9, 9, RED);
        x -= 16;

        String moneyS = fmt(ShopData.money);
        x -= font.width(moneyS);
        g.drawString(font, moneyS, x, y + 10, WHITE, false);
        x -= 13;
        g.fill(x, y + 9, x + 9, y + 18, 0xFF2E8B3E);
        g.drawString(font, "$", x + 2, y + 10, WHITE, false);
        x -= 16;

        int w = font.width(ShopData.vip) + 8;
        x -= w;
        g.fill(x, y + 8, x + w, y + 19, 0xFFD9A81E);
        g.drawString(font, ShopData.vip, x + 4, y + 10, 0xFF1A1405, false);
        return x;
    }
}
