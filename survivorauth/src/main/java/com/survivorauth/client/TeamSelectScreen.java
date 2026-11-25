package com.survivorauth.client;

import java.util.List;
import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import com.survivorauth.SurvivorAuth;
import com.survivorauth.config.ClientConfig;
import com.survivorauth.net.Team;
import com.survivorauth.net.TeamChoosePayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Escolha de time (Leste, Oeste, Norte, Sul). Aparece UMA vez, no primeiro acesso da conta,
 * entre o login e o lobby. A escolha é gravada no servidor e não pode ser trocada.
 *
 * Quatro cartões altos, cada um com a cor do time, uma bússola animada apontando para a
 * direção dele, nome, lema e descrição. Passar o mouse acende o cartão; clicar seleciona;
 * confirmar (botão ou ENTER) envia ao servidor.
 *
 * Textos: chaves survivorauth.team.* em lang/<idioma>.json.
 */
public class TeamSelectScreen extends Screen {
    private static final Team[] TEAMS = Team.values();
    private static final int N = TEAMS.length;

    // direção da seta de cada time (dx, dy) na bússola
    private static final int[][] DIR = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}}; // LESTE, OESTE, NORTE, SUL
    private static final String[] CARDINAL = {"L", "O", "N", "S"};
    private static final int[][] CARD_OFF = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}};

    // estado
    private long endTimeMs;
    private final long openMs = System.currentTimeMillis();
    private long lastFrameMs = openMs;
    private int selected = -1;
    private int hover = -1;
    private boolean busy;
    private final float[] hoverAnim = new float[N];
    private final float[] selAnim = new float[N];

    // tema
    private int accent, textColor, dim, panel;
    private float darkness;
    private boolean scanlines, vignette, flicker;
    private Scenery.Tex bgTex;
    private int imageX, imageY, imageW, imageH;
    private static final int[] CARD_COLORS = {0xFF8BC65A, 0xFF42B9E8, 0xFFFF713D, 0xFFB66AD8};

    // layout
    private final int[][] cardRect = new int[N][4];   // x, y, w, h
    private final int[] confirmRect = new int[4];
    private final int[] leaveRect = new int[4];
    private int titleY, subY, idY;
    private float titleScale;

    public TeamSelectScreen(int seconds) {
        super(Component.translatable("survivorauth.team.title"));
        this.endTimeMs = System.currentTimeMillis() + seconds * 1000L;
        accent = Scenery.parseColor(ClientConfig.ACCENT_COLOR.get(), 0xFFA3321F);
        textColor = Scenery.parseColor(ClientConfig.TEXT_COLOR.get(), 0xFFC9C5B8);
        dim = Scenery.parseColor(ClientConfig.DIM_COLOR.get(), 0xFF7A7D70);
        panel = Scenery.parseColor(ClientConfig.PANEL_COLOR.get(), 0xFF0C0E0B);
        darkness = ClientConfig.BACKGROUND_DARKNESS.get().floatValue();
        scanlines = ClientConfig.SCANLINES.get();
        vignette = ClientConfig.VIGNETTE.get();
        flicker = ClientConfig.FLICKER.get();
        bgTex = Scenery.probe(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "textures/gui/team_select_reference.png"));
    }

    // ------------------------------------------------------------------ servidor

    /** O servidor repete o pedido a cada ~10s: renova o relógio e destrava o botão. */
    public void applyState(int seconds) {
        if (seconds > 0) endTimeMs = System.currentTimeMillis() + seconds * 1000L;
        busy = false;
    }

    public void applyCounts() {
        // As contagens são atualizadas pelo servidor durante a fase de escolha.
    }

    // ------------------------------------------------------------------ layout

    @Override
    protected void init() {
        // A arte original tem proporção 3:2. Mantemos a proporção para não
        // deformar os cartões e calculamos as áreas clicáveis sobre a própria arte.
        float scale = Math.min(width / 1536f, height / 1024f);
        imageW = Math.round(1536f * scale);
        imageH = Math.round(1024f * scale);
        imageX = (width - imageW) / 2;
        imageY = (height - imageH) / 2;

        int[][] source = {
            {270, 350, 225, 447},
            {520, 350, 222, 447},
            {767, 350, 221, 447},
            {1009, 350, 214, 447}
        };
        for (int i = 0; i < N; i++) {
            int x = imageX + Math.round(source[i][0] * imageW / 1536f);
            int y = imageY + Math.round(source[i][1] * imageH / 1024f);
            int w = Math.round(source[i][2] * imageW / 1536f);
            int h = Math.round(source[i][3] * imageH / 1024f);
            setRect(cardRect[i], x, y, w, h);
        }
        // Cabeçalho responsivo sobre a arte.
        titleY = Math.max(28, imageY + Math.round(175 * imageH / 1024f));
        subY = titleY + 30;
        idY = Math.max(12, imageY + Math.round(135 * imageH / 1024f));
        titleScale = Math.max(1.0f, Math.min(1.8f, width / 850f));
        // A confirmação é feita com ENTER ou clicando novamente no cartão.
        setRect(confirmRect, 0, 0, 0, 0);
        setRect(leaveRect, 0, 0, 0, 0);
    }

    private static void setRect(int[] r, int x, int y, int w, int h) {
        r[0] = x; r[1] = y; r[2] = w; r[3] = h;
    }

    private static boolean inside(int[] r, double mx, double my) {
        return mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
    }

    // ------------------------------------------------------------------ entrada

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // só sai escolhendo ou abandonando
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = 0; i < N; i++) {
                if (inside(cardRect[i], mx, my)) {
                    if (selected == i) confirm();
                    else select(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key >= GLFW.GLFW_KEY_1 && key < GLFW.GLFW_KEY_1 + N) {
            select(key - GLFW.GLFW_KEY_1);
            return true;
        }
        if (key >= GLFW.GLFW_KEY_KP_1 && key < GLFW.GLFW_KEY_KP_1 + N) {
            select(key - GLFW.GLFW_KEY_KP_1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) {
            select(selected < 0 ? 0 : Math.min(N - 1, selected + 1));
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) {
            select(selected < 0 ? N - 1 : Math.max(0, selected - 1));
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            confirm();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private void select(int i) {
        if (busy || i == selected) return;
        selected = i;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.8f + 0.12f * i));
    }

    private void confirm() {
        if (busy || selected < 0) return;
        busy = true;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0f));
        PacketDistributor.sendToServer(new TeamChoosePayload(TEAMS[selected].id));
    }

    private void leave() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) mc.level.disconnect();
        mc.disconnect();
        mc.setScreen(new TitleScreen());
    }

    // ------------------------------------------------------------------ desenho

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // desenhado em render(), junto com a animação
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        float dt = Math.min(0.1f, (now - lastFrameMs) / 1000f);
        lastFrameMs = now;

        hover = -1;
        for (int i = 0; i < N; i++) if (inside(cardRect[i], mouseX, mouseY)) hover = i;
        float k = Math.min(1f, dt * 12f);
        for (int i = 0; i < N; i++) {
            hoverAnim[i] += ((i == hover ? 1f : 0f) - hoverAnim[i]) * k;
            selAnim[i] += ((i == selected ? 1f : 0f) - selAnim[i]) * k;
        }

        drawScene(g);
        drawAmbientParticles(g, now);
        Font font = Minecraft.getInstance().font;
        drawHeader(g, font, now);
        drawTeamLabels(g, font, now);
        drawCardFocus(g, now);
        drawFooter(g, font, mouseX, mouseY);
    }

    /** Realça apenas o cartão ativo; o restante da composição vem da imagem de referência. */
    private void drawCardFocus(GuiGraphics g, long now) {
        for (int i = 0; i < N; i++) {
            float glow = Math.max(hoverAnim[i] * 0.42f, selAnim[i]);
            if (glow < 0.025f) continue;
            int[] r = cardRect[i];
            int col = CARD_COLORS[i] & 0xFFFFFF;
            int alpha = (int) (75 * glow);
            g.fill(r[0] + 3, r[1] + 3, r[0] + r[2] - 3, r[1] + r[3] - 3,
                    (alpha << 24) | col);
            int borderAlpha = Math.min(255, (int) (150 + 105 * glow));
            int border = (borderAlpha << 24) | col;
            for (int t = 0; t < 2; t++) {
                int inset = 1 + t;
                g.fill(r[0] + inset, r[1] + inset, r[0] + r[2] - inset, r[1] + inset + 1, border);
                g.fill(r[0] + inset, r[1] + r[3] - inset - 1, r[0] + r[2] - inset, r[1] + r[3] - inset, border);
                g.fill(r[0] + inset, r[1] + inset, r[0] + inset + 1, r[1] + r[3] - inset, border);
                g.fill(r[0] + r[2] - inset - 1, r[1] + inset, r[0] + r[2] - inset, r[1] + r[3] - inset, border);
            }
        }
        if (busy) {
            Font font = Minecraft.getInstance().font;
            Component working = Component.translatable("survivorauth.team.working");
            g.drawCenteredString(font, working, width / 2, height - 18, 0xFFE8E3D6);
        }
    }

    private void drawScene(GuiGraphics g) {
        g.fill(0, 0, width, height, 0xFF050706);
        if (bgTex != null) {
            Scenery.drawContain(g, bgTex, imageX, imageY, imageW, imageH);
        } else {
            Scenery.drawWilderness(g, width, height);
        }
    }

    /** Camadas de partículas, vaga-lumes, névoa e riscos de luz no cenário. */
    private void drawAmbientParticles(GuiGraphics g, long now) {
        // Partículas pequenas subindo pelo cenário.
        for (int i = 0; i < 115; i++) {
            double phase = i * 2.399963229728653;
            double speed = 0.00007 + (i % 7) * 0.000018;
            int x = (int) Math.floor((Math.sin(phase) * 0.5 + 0.5) * width
                    + Math.sin(now * speed + phase) * (10 + i % 18));
            int y = (int) Math.floor((Math.cos(phase * 1.7) * 0.5 + 0.5) * height
                    - (now * (0.004 + (i % 6) * 0.0018) + i * 23) % Math.max(1, height));
            float twinkle = 0.25f + 0.75f * (0.5f + 0.5f * (float) Math.sin(now / (180.0 + i * 5) + phase));
            int alpha = (int) ((i % 4 == 0 ? 145 : 82) * twinkle);
            int rgb = i % 4 == 0 ? 0xF4B76B : (i % 4 == 1 ? 0x8DD9FF : (i % 4 == 2 ? 0xB6E58A : 0xD7A8FF));
            int size = i % 11 == 0 ? 3 : (i % 3 == 0 ? 2 : 1);
            g.fill(x, y, x + size, y + size, (alpha << 24) | rgb);
        }

        // Vaga-lumes maiores com halo suave.
        for (int i = 0; i < 18; i++) {
            double phase = i * 2.17;
            int x = (int) (width * (0.08 + 0.84 * ((Math.sin(now / 2600.0 + phase) + 1) / 2)));
            int y = (int) (height * (0.18 + 0.68 * ((Math.cos(now / 3100.0 + phase * 1.3) + 1) / 2)));
            int pulse = (int) (55 + 75 * (0.5 + 0.5 * Math.sin(now / 230.0 + phase)));
            int rgb = i % 2 == 0 ? 0xE9A85C : 0x8CCFE8;
            g.fill(x - 4, y - 4, x + 5, y + 5, (pulse / 5 << 24) | rgb);
            g.fill(x - 1, y - 1, x + 2, y + 2, (pulse << 24) | 0xFFF0C2);
        }

        // Feixes finos atravessam lentamente o fundo, sem cobrir os cartões.
        int sweepX = (int) ((now / 22L) % (width + 220)) - 110;
        g.fillGradient(sweepX, 0, sweepX + 2, height, 0x00000000, 0x244D7890);
        int sweepY = (int) ((now / 35L) % Math.max(1, height));
        g.fill(0, sweepY, width, sweepY + 1, 0x123C6470);
    }

    /** Atualiza somente os textos variáveis; a arte original dos cartões permanece intacta. */
    private void drawTeamLabels(GuiGraphics g, Font font, long now) {
        String[] names = {"LESTE", "OESTE", "NORTE", "SUL"};
        float sy = imageH / 1024f;
        for (int i = 0; i < N; i++) {
            int[] r = cardRect[i];
            int cx = r[0] + r[2] / 2;
            int rgb = CARD_COLORS[i] & 0xFFFFFF;
            int nameTop = imageY + Math.round(420 * sy);
            int nameBottom = imageY + Math.round(488 * sy);
            int countTop = imageY + Math.round(724 * sy);
            int countBottom = imageY + Math.round(780 * sy);
            // placas escuras discretas apagam os textos antigos da imagem sem redesenhar o cartão.
            g.fill(r[0] + 7, nameTop, r[0] + r[2] - 7, nameBottom, 0xB9000000);
            g.fill(r[0] + 7, countTop, r[0] + r[2] - 7, countBottom, 0xC9000000);
            float pulse = 0.88f + 0.12f * (float) Math.sin(now / 520.0 + i);
            int text = ((int) (255 * pulse) << 24) | rgb;
            Component teamName = Component.literal(names[i]);
            g.drawCenteredString(font, teamName, cx, nameTop + 9, text);
            int count = ClientTeamCounts.get(i);
            Component members = Component.literal(count + (count == 1 ? " JOGADOR" : " JOGADORES"));
            g.drawCenteredString(font, members, cx, countTop + 20, 0xFFF0EBDD);
        }
    }

    private void drawHeader(GuiGraphics g, Font font, long now) {
        String name = Minecraft.getInstance().getUser().getName();
        Component id = Component.translatable("survivorauth.id", name);
        g.drawCenteredString(font, id, width / 2, idY, dim);

        Component title = Component.translatable("survivorauth.team.title");
        float pulse = 1f;
        if (flicker) {
            pulse = (float) (0.92 + 0.08 * Math.sin(now / 420.0));
            if ((now / 70) % 61 == 0) pulse *= 0.5f;
        }
        int tw = font.width(title);
        g.pose().pushPose();
        g.pose().translate(width / 2f, (float) titleY, 0f);
        g.pose().scale(titleScale, titleScale, 1f);
        g.drawString(font, title, -tw / 2, 0, Scenery.withAlpha(0xF2EEE4, Math.max(70, (int) (255 * pulse))), false);
        g.pose().popPose();

        // traço central que pega a cor do time em foco
        int barY = titleY + Math.round(9 * titleScale) + 4;
        int barCol = accent;
        int focus = selected >= 0 ? selected : hover;
        if (focus >= 0) barCol = TEAMS[focus].color;
        g.fill(width / 2 - 20, barY, width / 2 + 20, barY + 2, barCol);

        // aviso de que a escolha é definitiva
        Component sub = Component.translatable("survivorauth.team.subtitle");
        List<FormattedCharSequence> lines = font.split(sub, Math.min(width - 24, 420));
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            FormattedCharSequence l = lines.get(i);
            g.drawString(font, l, (width - font.width(l)) / 2, subY + i * 10, 0xFFD9604A, false);
        }
    }

    // ---------------------------------------------------------------- cartões

    private void drawCard(GuiGraphics g, Font font, int i, long now) {
        Team team = TEAMS[i];
        int col = team.color;
        int rgb = col & 0xFFFFFF;
        int[] r = cardRect[i];

        float reveal = ease(clamp01((now - openMs - i * 110L) / 450f));
        if (reveal < 0.06f) return;

        float hv = hoverAnim[i], sv = selAnim[i], glow = Math.max(hv, sv);
        int lift = Math.round(hv * 5f + sv * 8f);
        int x = r[0], w = r[2], h = r[3];
        int y = r[1] + Math.round((1f - reveal) * 28f) - lift;

        // sombra suave para separar o cartão do cenário
        g.fill(x - 2, y + 3, x + w + 2, y + h + 4, 0x65000000);

        // aura em volta (pulsa quando selecionado)
        float pulse = sv > 0.01f ? (float) (0.75 + 0.25 * Math.sin(now / 280.0)) : 1f;
        for (int s = 1; s <= 7; s++) {
            float a = (sv * 0.55f * pulse + hv * 0.22f) * (float) Math.pow(1.0 - s / 8.0, 2) * reveal;
            if (a < 0.01f) continue;
            int c = ((int) (255 * a) << 24) | rgb;
            g.fill(x - s, y - s, x + w + s, y - s + 1, c);
            g.fill(x - s, y + h + s - 1, x + w + s, y + h + s, c);
            g.fill(x - s, y - s, x - s + 1, y + h + s, c);
            g.fill(x + w + s - 1, y - s, x + w + s, y + h + s, c);
        }

        // corpo
        g.fill(x, y, x + w, y + h, ((int) (0xE0 * reveal) << 24) | (panel & 0xFFFFFF));
        g.fillGradient(x, y, x + w, y + h * 6 / 10,
                (int) ((0x26 + 0x40 * glow) * reveal) << 24 | rgb, rgb);
        g.fillGradient(x, y + h * 55 / 100, x + w, y + h,
                rgb, (int) ((0x24 + 0x30 * sv) * reveal) << 24 | rgb);
        // faixa inferior animada, que reage à seleção
        int baseLineY = y + h - 3;
        int lineW = Math.max(12, (int) ((w - 16) * (0.22f + 0.78f * Math.max(hv, sv))));
        float linePulse = 0.72f + 0.28f * (float) Math.sin(now / 300.0 + i);
        int lineColor = ((int) (190 * reveal * linePulse) << 24) | rgb;
        g.fill(x + 8, baseLineY, x + 8 + lineW, baseLineY + 1, lineColor);

        // moldura + barra superior
        int border = (int) (255 * reveal * (0.28f + 0.72f * sv + 0.35f * hv)) ;
        border = Math.min(255, border) << 24 | rgb;
        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + h - 1, x + w, y + h, border);
        g.fill(x, y, x + 1, y + h, border);
        g.fill(x + w - 1, y, x + w, y + h, border);
        g.fill(x, y, x + w, y + 3, ((int) (255 * reveal * (0.6f + 0.4f * glow)) << 24) | rgb);

        // reflexo percorre o cartão continuamente; fica mais intenso ao passar o mouse
        int shimmerW = Math.max(5, w / 7);
        int shimmerX = x - shimmerW + (int) ((now / 12L + i * 83L) % (w + shimmerW));
        int shimmerAlpha = (int) ((12 + 25 * glow) * reveal);
        int shimmerLeft = Math.max(x + 1, shimmerX);
        int shimmerRight = Math.min(x + w - 1, shimmerX + shimmerW);
        if (shimmerAlpha > 0 && shimmerRight > shimmerLeft) {
            g.fillGradient(shimmerLeft, y + 4, shimmerRight, y + h - 4,
                    (shimmerAlpha << 24) | 0xFFFFFF, 0x00FFFFFF);
        }

        // cantos iluminados reforçam o acabamento do cartão
        int cornerAlpha = (int) ((65 + 150 * glow) * reveal);
        int corner = (cornerAlpha << 24) | rgb;
        int mark = Math.max(7, Math.min(15, w / 9));
        g.fill(x + 5, y + 5, x + 5 + mark, y + 6, corner);
        g.fill(x + 5, y + 5, x + 6, y + 5 + mark, corner);
        g.fill(x + w - 5 - mark, y + h - 6, x + w - 5, y + h - 5, corner);
        g.fill(x + w - 6, y + h - 5 - mark, x + w - 5, y + h - 5, corner);

        // número do atalho
        g.drawString(font, String.valueOf(i + 1), x + w - 10, y + 7,
                Scenery.withAlpha(0xFFFFFF, (int) (90 * reveal) + 8), false);

        // bússola
        int rad = Math.max(14, Math.min(34, Math.min(w * 26 / 100, h * 16 / 100)));
        int cx = x + w / 2;
        int cy = y + 24 + rad;
        drawCompass(g, font, cx, cy, rad, i, col, glow, reveal, now);

        // nome
        Component name = Component.translatable("survivorauth.team." + team.id);
        float ns = Math.max(1.2f, Math.min(2.2f, (w - 16) / (float) Math.max(1, font.width(name))));
        int ny = cy + rad + 20;
        g.pose().pushPose();
        g.pose().translate((float) cx, (float) ny, 0f);
        g.pose().scale(ns, ns, 1f);
        g.drawString(font, name, -font.width(name) / 2, 0,
                Scenery.withAlpha(0xF2EEE4, (int) (255 * reveal)), false);
        g.pose().popPose();

        // sublinhado que cresce com hover/seleção
        int uy = ny + Math.round(9 * ns) + 3;
        int uw = 14 + Math.round((w - 28 - 14) * glow);
        g.fill(cx - uw / 2, uy, cx + uw / 2, uy + 2, ((int) (255 * reveal) << 24) | rgb);

        // lema + descrição
        int ty = uy + 9;
        int tx = x + 8;
        int maxW = w - 16;
        Component motto = Component.translatable("survivorauth.team." + team.id + ".motto");
        for (FormattedCharSequence l : font.split(motto, maxW)) {
            if (ty > y + h - 28) break;
            g.drawString(font, l, cx - font.width(l) / 2, ty, Scenery.withAlpha(rgb, (int) (255 * reveal)), false);
            ty += 10;
        }
        ty += 3;
        Component desc = Component.translatable("survivorauth.team." + team.id + ".desc");
        for (FormattedCharSequence l : font.split(desc, maxW)) {
            if (ty > y + h - 24) break;
            g.drawString(font, l, cx - font.width(l) / 2, ty, Scenery.withAlpha(textColor, (int) (210 * reveal)), false);
            ty += 9;
        }

        // quantidade real de contas vinculadas ao time, enviada pelo servidor
        Component members = Component.literal(ClientTeamCounts.get(i) + " SOBREVIVENTES");
        int countY = y + h - 29;
        g.drawCenteredString(font, members, cx, countY,
                Scenery.withAlpha(0xE8E3D6, (int) (235 * reveal)));

        // selo "SELECIONADO"
        if (sv > 0.3f) {
            Component tag = Component.translatable("survivorauth.team.selected");
            int tw = font.width(tag) + 12;
            int by = y + h - 17;
            g.fill(cx - tw / 2, by, cx + tw / 2, by + 12, ((int) (255 * sv * reveal) << 24) | rgb);
            g.drawString(font, tag, cx - font.width(tag) / 2, by + 2,
                    Scenery.withAlpha(onColor(col) & 0xFFFFFF, (int) (255 * sv * reveal) | 8), false);
        }
    }

    /** Bússola do time: anel, pontas cardeais, seta na direção dele e um ponto orbitando quando aceso. */
    private void drawCompass(GuiGraphics g, Font font, int cx, int cy, int r, int idx, int col,
                             float glow, float reveal, long now) {
        int rgb = col & 0xFFFFFF;
        int[] dir = DIR[idx];

        // disco de fundo
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1,
                    ((int) (255 * (0.07f + 0.12f * glow) * reveal) << 24) | rgb);
        }
        ring(g, cx, cy, r, ((int) (255 * (0.40f + 0.55f * glow) * reveal) << 24) | rgb);
        ring(g, cx, cy, r - 4, ((int) (255 * (0.12f + 0.25f * glow) * reveal) << 24) | rgb);

        // pontas cardeais: a do time é longa e acesa, as outras são curtas e apagadas
        for (int d = 0; d < N; d++) {
            boolean mine = d == idx;
            int len = mine ? 6 : 3;
            int ox = CARD_OFF[d][0], oy = CARD_OFF[d][1];
            int a = (int) (255 * reveal * (mine ? 1f : 0.45f));
            int c = (a << 24) | (mine ? rgb : (dim & 0xFFFFFF));
            int x1 = cx + ox * (r - len), y1 = cy + oy * (r - len);
            int x2 = cx + ox * (r + 1), y2 = cy + oy * (r + 1);
            g.fill(Math.min(x1, x2) - (oy != 0 ? 1 : 0), Math.min(y1, y2) - (ox != 0 ? 1 : 0),
                    Math.max(x1, x2) + 1 + (oy != 0 ? 1 : 0), Math.max(y1, y2) + 1 + (ox != 0 ? 1 : 0), c);

            // letra fora do anel
            String letter = CARDINAL[d];
            int lx = cx + ox * (r + 8) - font.width(letter) / 2;
            int ly = cy + oy * (r + 8) - 4;
            g.drawString(font, letter, lx, ly, (Math.max(8, (int) (255 * reveal * (mine ? 1f : 0.5f))) << 24)
                    | (mine ? rgb : (dim & 0xFFFFFF)), false);
        }

        // seta apontando para a direção do time
        float tip = r * 0.72f, headBase = r * 0.12f, tail = -r * 0.55f;
        int arrow = ((int) (255 * reveal * (0.8f + 0.2f * glow)) << 24) | rgb;
        for (int s = Math.round(tail); s <= Math.round(tip); s++) {
            int half = s <= headBase ? 1 : Math.round((tip - s) * 0.62f);
            if (dir[0] != 0) {
                int px = cx + dir[0] * s;
                g.fill(px, cy - half, px + 1, cy + half + 1, arrow);
            } else {
                int py = cy + dir[1] * s;
                g.fill(cx - half, py, cx + half + 1, py + 1, arrow);
            }
        }

        // ponto orbitando quando aceso
        if (glow > 0.05f) {
            double ang = now / 700.0 + idx * 1.7;
            int px = cx + (int) Math.round(Math.cos(ang) * r), py = cy + (int) Math.round(Math.sin(ang) * r);
            int c = ((int) (255 * glow * reveal) << 24) | 0xFFFFFF;
            g.fill(px - 1, py - 1, px + 2, py + 2, c);
        }
    }

    private static void ring(GuiGraphics g, int cx, int cy, int r, int color) {
        int steps = Math.max(24, (int) (2 * Math.PI * r));
        for (int i = 0; i < steps; i++) {
            double a = i * 2 * Math.PI / steps;
            int px = cx + (int) Math.round(Math.cos(a) * r);
            int py = cy + (int) Math.round(Math.sin(a) * r);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    // ---------------------------------------------------------------- botão e rodapé

    private void drawConfirm(GuiGraphics g, Font font, int mx, int my, long now) {
        int[] r = confirmRect;
        boolean ready = selected >= 0 && !busy;
        boolean hot = ready && inside(r, mx, my);

        Component label;
        int fill, text;
        if (selected < 0) {
            label = Component.translatable("survivorauth.team.pick");
            fill = Scenery.withAlpha(dim, 0x40);
            text = Scenery.withAlpha(textColor, 0x90);
        } else {
            Team t = TEAMS[selected];
            int base = t.color;
            if (busy) {
                label = Component.translatable("survivorauth.team.working");
                fill = Scenery.withAlpha(base, 0x80);
                text = Scenery.withAlpha(onColor(base), 0xB0);
            } else {
                label = Component.translatable("survivorauth.team.confirm",
                        Component.translatable("survivorauth.team." + t.id));
                fill = hot ? Scenery.lighten(base) : base;
                text = onColor(base);
                // contorno pulsante chamando atenção para o botão
                float p = (float) (0.5 + 0.5 * Math.sin(now / 350.0));
                int oc = ((int) (140 * p) << 24) | (base & 0xFFFFFF);
                g.fill(r[0] - 2, r[1] - 2, r[0] + r[2] + 2, r[1] - 1, oc);
                g.fill(r[0] - 2, r[1] + r[3] + 1, r[0] + r[2] + 2, r[1] + r[3] + 2, oc);
                g.fill(r[0] - 2, r[1] - 2, r[0] - 1, r[1] + r[3] + 2, oc);
                g.fill(r[0] + r[2] + 1, r[1] - 2, r[0] + r[2] + 2, r[1] + r[3] + 2, oc);
            }
        }
        g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], fill);
        g.drawCenteredString(font, label, r[0] + r[2] / 2, r[1] + (r[3] - 8) / 2, text);

        g.drawCenteredString(font, Component.translatable("survivorauth.team.hint"),
                width / 2, r[1] + r[3] + 6, Scenery.withAlpha(dim, 0xB0));
    }

    private void drawFooter(GuiGraphics g, Font font, int mx, int my) {
        long left = Math.max(0L, (endTimeMs - System.currentTimeMillis()) / 1000L);
        String time = String.format(Locale.ROOT, "%02d:%02d", left / 60, left % 60);
        boolean blinkOff = left <= 15 && (System.currentTimeMillis() / 400) % 2 == 0;
        Component timer = Component.translatable("survivorauth.timer", time);
        g.drawString(font, timer, width - 24 - font.width(timer), height - 18,
                blinkOff ? 0xFFFF4040 : Scenery.withAlpha(textColor, 0xC0), false);
        Component countNote = Component.literal("MEMBROS CADASTRADOS POR TIME");
        g.drawCenteredString(font, countNote, width / 2, height - 18, Scenery.withAlpha(dim, 0xC0));
    }

    // ---------------------------------------------------------------- utilitários

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    /** Desaceleração suave (cubic ease-out). */
    private static float ease(float t) {
        float u = 1f - t;
        return 1f - u * u * u;
    }

    /** Texto escuro sobre cores claras, claro sobre cores escuras. */
    private static int onColor(int argb) {
        int r = (argb >> 16) & 255, gg = (argb >> 8) & 255, b = argb & 255;
        return (0.30 * r + 0.59 * gg + 0.11 * b) > 140 ? 0xFF0C0E0B : 0xFFF2EEE4;
    }
}
