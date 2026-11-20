package com.survivorauth.client;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.NativeImage;
import com.survivorauth.SurvivorAuth;
import com.survivorauth.config.ClientConfig;
import com.survivorauth.net.AuthState;
import com.survivorauth.net.AuthSubmitPayload;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Tela de login no estilo do menu do DayZ: coluna escura à esquerda que se dissolve na cena,
 * título grande alinhado à esquerda, campos planos com sublinhado e um botão sólido.
 *
 * Personalização (sem mexer em código):
 *  - Fundo:  assets/survivorauth/textures/gui/background.png  (resource pack)
 *  - Logo:   assets/survivorauth/textures/gui/logo.png        (resource pack)
 *  - Textos: assets/survivorauth/lang/<idioma>.json           (resource pack)
 *  - Cores/efeitos: config/survivorauth-client.toml
 */
public class LoginScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "textures/gui/background.png");
    private static final ResourceLocation LOGO =
            ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "textures/gui/logo.png");

    private static final int FIELD_H = 20;

    private static final int STYLE_PRIMARY = 0;
    private static final int STYLE_TEXT = 1;
    private static final int STYLE_TEXT_LEFT = 2;

    private record Tex(ResourceLocation loc, int w, int h) {}

    // estado
    private boolean registerMode;
    private long endTimeMs;
    private boolean busy;
    private boolean showPassword;
    private Component status = Component.empty();
    private String keepPass = "";
    private String keepConfirm = "";

    // widgets
    private EditBox passBox;
    private EditBox confirmBox;
    private DayButton mainBtn;
    private DayButton showBtn;

    // layout (calculado em init)
    private int colW, x0, fieldW;
    private int titleY, titleH, accentY, subY, idY, l1Y, f1Y, l2Y, f2Y, showY, statusY, mainY, leaveY;
    private float titleScale;

    // tema
    private int accent, textColor, dim, panel;
    private float darkness;
    private boolean scanlines, vignette, flicker;
    private Tex bgTex, logoTex;

    public LoginScreen(boolean registerMode, int seconds) {
        super(Component.translatable("survivorauth.title"));
        this.registerMode = registerMode;
        this.endTimeMs = System.currentTimeMillis() + seconds * 1000L;
        loadTheme();
    }

    // ------------------------------------------------------------------ tema

    private void loadTheme() {
        accent = parseColor(ClientConfig.ACCENT_COLOR.get(), 0xFFA3321F);
        textColor = parseColor(ClientConfig.TEXT_COLOR.get(), 0xFFC9C5B8);
        dim = parseColor(ClientConfig.DIM_COLOR.get(), 0xFF7A7D70);
        panel = parseColor(ClientConfig.PANEL_COLOR.get(), 0xFF0C0E0B);
        darkness = ClientConfig.BACKGROUND_DARKNESS.get().floatValue();
        scanlines = ClientConfig.SCANLINES.get();
        vignette = ClientConfig.VIGNETTE.get();
        flicker = ClientConfig.FLICKER.get();
        bgTex = probe(BACKGROUND);
        logoTex = probe(LOGO);
    }

    private static int parseColor(String value, int fallback) {
        try {
            String h = value.trim();
            if (h.startsWith("#")) h = h.substring(1);
            return 0xFF000000 | (Integer.parseInt(h, 16) & 0xFFFFFF);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static int lighten(int c) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        r += (255 - r) / 5;
        g += (255 - g) / 5;
        b += (255 - b) / 5;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Devolve a textura se existir (do mod ou de um resource pack), senão null. */
    private static Tex probe(ResourceLocation loc) {
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(loc);
            if (resource.isEmpty()) return null;
            try (InputStream in = resource.get().open(); NativeImage img = NativeImage.read(in)) {
                return new Tex(loc, img.getWidth(), img.getHeight());
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ init / layout

    @Override
    protected void init() {
        if (passBox != null) keepPass = passBox.getValue();
        if (confirmBox != null) keepConfirm = confirmBox.getValue();

        Font font = Minecraft.getInstance().font;

        colW = Math.min(width, Math.min(380, Math.max(280, (int) (width * 0.36f))));
        x0 = colW >= 300 ? 36 : 24;
        fieldW = colW - x0 * 2;

        // título: escala para caber na coluna
        int tw = Math.max(1, font.width(Component.translatable("survivorauth.title")));
        titleScale = Math.max(1.5f, Math.min(3f, fieldW / (float) tw));
        titleH = logoTex != null ? 34 : Math.round(9 * titleScale);

        // posições relativas ao topo do bloco
        int c = 0;
        titleY = c;                 c += titleH + 8;
        accentY = c;                c += 2 + 12;
        subY = c;                   c += 9 + 5;
        idY = c;                    c += 9 + 22;
        l1Y = c;                    c += 11;
        f1Y = c;                    c += FIELD_H;
        if (registerMode) {
            c += 14;
            l2Y = c;                c += 11;
            f2Y = c;                c += FIELD_H;
        }
        showY = c + 6;              c += 6 + 12;
        statusY = c + 6;            c += 6 + 20;
        mainY = c + 8;              c += 8 + 22;
        leaveY = c + 8;             c += 8 + 14;

        int top = Math.max(14, (height - c) / 2);
        titleY += top; accentY += top; subY += top; idY += top; l1Y += top; f1Y += top;
        l2Y += registerMode ? top : 0; f2Y += registerMode ? top : 0;
        showY += top; statusY += top; mainY += top; leaveY += top;

        passBox = newBox(f1Y, "survivorauth.label.password", "survivorauth.hint.password");
        passBox.setValue(keepPass);
        passBox.setResponder(s -> keepPass = s);
        addRenderableWidget(passBox);

        if (registerMode) {
            confirmBox = newBox(f2Y, "survivorauth.label.confirm", "survivorauth.hint.confirm");
            confirmBox.setValue(keepConfirm);
            confirmBox.setResponder(s -> keepConfirm = s);
            addRenderableWidget(confirmBox);
        } else {
            confirmBox = null;
        }

        showBtn = addRenderableWidget(new DayButton(x0, showY, 130, 12, showLabel(), STYLE_TEXT_LEFT, this::toggleShow));
        mainBtn = addRenderableWidget(new DayButton(x0, mainY, fieldW, 22, mainLabel(), STYLE_PRIMARY, this::submit));
        mainBtn.active = !busy;
        addRenderableWidget(new DayButton(x0, leaveY, fieldW, 14,
                Component.translatable("survivorauth.button.leave"), STYLE_TEXT, this::leave));

        setInitialFocus(passBox);
    }

    private EditBox newBox(int fieldY, String labelKey, String hintKey) {
        EditBox box = new EditBox(Minecraft.getInstance().font, x0 + 8, fieldY + (FIELD_H - 8) / 2,
                fieldW - 16, 8, Component.translatable(labelKey));
        box.setBordered(false);
        box.setMaxLength(64);
        box.setTextColor(textColor);
        box.setHint(Component.translatable(hintKey).withStyle(ChatFormatting.DARK_GRAY));
        box.setFormatter((text, offset) ->
                FormattedCharSequence.forward(showPassword ? text : "*".repeat(text.length()), Style.EMPTY));
        return box;
    }

    private Component showLabel() {
        return Component.translatable(showPassword ? "survivorauth.button.hide" : "survivorauth.button.show");
    }

    private Component mainLabel() {
        if (busy) return Component.translatable("survivorauth.button.working");
        return Component.translatable(registerMode ? "survivorauth.button.register" : "survivorauth.button.login");
    }

    // ------------------------------------------------------------------ ações

    private void toggleShow() {
        showPassword = !showPassword;
        showBtn.setMessage(showLabel());
    }

    private void submit() {
        if (busy) return;
        String pw = passBox.getValue();
        if (pw.isEmpty()) {
            status = Component.translatable("survivorauth.status.empty");
            return;
        }
        String confirm = registerMode ? confirmBox.getValue() : "";
        if (registerMode && !pw.equals(confirm)) {
            status = Component.translatable("survivorauth.status.mismatch");
            return;
        }
        busy = true;
        status = Component.empty();
        refreshMain();
        PacketDistributor.sendToServer(new AuthSubmitPayload(registerMode, pw, confirm));
    }

    private void leave() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) mc.level.disconnect();
        mc.disconnect();
        mc.setScreen(new TitleScreen());
    }

    private void refreshMain() {
        if (mainBtn == null) return;
        mainBtn.setMessage(mainLabel());
        mainBtn.active = !busy;
    }

    /** Chamado quando o servidor manda um novo estado (senha errada, abrir em outro modo, etc.). */
    public void applyState(int state, int arg, int seconds) {
        busy = false;
        switch (state) {
            case AuthState.PROMPT_LOGIN, AuthState.PROMPT_REGISTER -> {
                boolean reg = state == AuthState.PROMPT_REGISTER;
                if (seconds > 0) endTimeMs = System.currentTimeMillis() + seconds * 1000L;
                if (reg != registerMode) {
                    registerMode = reg;
                    rebuildWidgets();
                }
            }
            case AuthState.WRONG_PASSWORD -> {
                status = Component.translatable("survivorauth.status.wrong", arg);
                keepPass = "";
                if (passBox != null) passBox.setValue("");
            }
            case AuthState.TOO_SHORT -> status = Component.translatable("survivorauth.status.short", arg);
            case AuthState.MISMATCH -> status = Component.translatable("survivorauth.status.mismatch");
            case AuthState.LOCKED -> status = Component.translatable("survivorauth.status.locked", arg);
            case AuthState.BUSY -> status = Component.translatable("survivorauth.status.busy");
            default -> { }
        }
        refreshMain();
    }

    // ------------------------------------------------------------------ comportamento da tela

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // só sai logando ou abandonando
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        boolean enter = key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER;
        if (enter && !(getFocused() instanceof DayButton)) {
            submit();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    // ------------------------------------------------------------------ desenho

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        drawScene(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick); // fundo + campos + widgets
        drawTexts(g);
    }

    private void drawScene(GuiGraphics g) {
        int w = width;
        int h = height;

        g.fill(0, 0, w, h, 0xFF080907);

        if (bgTex != null) {
            // imagem do resource pack, cobrindo a tela sem distorcer
            float scale = Math.max((float) w / bgTex.w(), (float) h / bgTex.h());
            int dw = Math.round(bgTex.w() * scale);
            int dh = Math.round(bgTex.h() * scale);
            g.enableScissor(0, 0, w, h);
            g.blit(bgTex.loc(), (w - dw) / 2, (h - dh) / 2, dw, dh, 0f, 0f, bgTex.w(), bgTex.h(), bgTex.w(), bgTex.h());
            g.disableScissor();
            g.fill(0, 0, w, h, ((int) (Math.min(1f, Math.max(0f, darkness)) * 255f)) << 24);
        } else {
            drawWilderness(g, w, h);
        }

        // coluna escura à esquerda que se dissolve na cena (como o menu do DayZ)
        int fade = colW + 100;
        for (int x = 0; x < fade; x += 2) {
            double a = x < colW ? 1.0 : Math.pow(1.0 - (x - colW) / (double) (fade - colW), 2);
            g.fill(x, 0, x + 2, h, withAlpha(panel, (int) (0xE8 * a)));
        }

        if (vignette) {
            int steps = 24;
            int sx = Math.max(1, w / 90);
            int sy = Math.max(1, h / 60);
            for (int i = 0; i < steps; i++) {
                int a = (int) (110 * Math.pow(1.0 - i / (double) steps, 2));
                int c = a << 24;
                g.fill(w - (i + 1) * sx, 0, w - i * sx, h, c);
                g.fill(0, i * sy, w, (i + 1) * sy, c);
                g.fill(0, h - (i + 1) * sy, w, h - i * sy, c);
            }
        }

        if (scanlines) {
            for (int y = 0; y < h; y += 3) g.fill(0, y, w, y + 1, 0x14000000);
        }

        // campos planos com sublinhado
        drawField(g, f1Y, passBox != null && passBox.isFocused());
        if (registerMode) drawField(g, f2Y, confirmBox != null && confirmBox.isFocused());
    }

    /** Cenário procedural: céu nublado, floresta de pinheiros em duas camadas e neblina. */
    private void drawWilderness(GuiGraphics g, int w, int h) {
        g.fillGradient(0, 0, w, h * 80 / 100, 0xFF1A1D18, 0xFF4B5247);

        drawPines(g, w, h * 80 / 100, Math.max(12, h * 20 / 100), 11, 3, 0xFF262C22);

        g.fillGradient(0, h * 58 / 100, w, h * 84 / 100, 0x00000000, 0x77434B3E);
        g.fillGradient(0, h * 80 / 100, w, h, 0xFF181C14, 0xFF070806);

        drawPines(g, w, h + h / 50, Math.max(20, h * 36 / 100), 26, 7, 0xFF0A0C09);
    }

    private void drawPines(GuiGraphics g, int w, int baseY, int treeH, int spacing, int seed, int color) {
        for (int x = -10, i = 0; x < w + 20; x += spacing, i++) {
            int hash = (i * 73856093) ^ (seed * 19349663);
            int th = treeH * (70 + Math.floorMod(hash >> 3, 50)) / 100;
            int tx = x + Math.floorMod(hash >> 9, spacing / 2 + 1);
            for (int r = 0; r < th; r += 2) {
                int half = 1 + (int) (r * 0.30f) + ((r >> 1) & 1);
                g.fill(tx - half, baseY - th + r, tx + half + 1, baseY - th + r + 2, color);
            }
            g.fill(tx - 1, baseY - 3, tx + 1, baseY, color);
        }
    }

    private void drawField(GuiGraphics g, int y, boolean focused) {
        g.fill(x0, y, x0 + fieldW, y + FIELD_H, withAlpha(0xFFFFFF, 0x10));
        g.fill(x0, y + FIELD_H - 1, x0 + fieldW, y + FIELD_H, focused ? accent : withAlpha(dim, 0x90));
        if (focused) g.fill(x0, y, x0 + 2, y + FIELD_H, accent);
    }

    private void drawTexts(GuiGraphics g) {
        Font font = Minecraft.getInstance().font;

        // título grande à esquerda (ou logo, se o resource pack trouxe uma)
        if (logoTex != null) {
            float s = Math.min((float) titleH / logoTex.h(), (float) fieldW / logoTex.w());
            int lw = Math.round(logoTex.w() * s);
            int lh = Math.round(logoTex.h() * s);
            g.blit(logoTex.loc(), x0, titleY, lw, lh, 0f, 0f, logoTex.w(), logoTex.h(), logoTex.w(), logoTex.h());
        } else {
            float pulse = 1f;
            if (flicker) {
                long t = System.currentTimeMillis();
                pulse = (float) (0.9 + 0.1 * Math.sin(t / 420.0));
                if ((t / 70) % 61 == 0) pulse *= 0.45f;
            }
            int titleColor = withAlpha(textColor, Math.max(60, (int) (255 * pulse)));
            g.pose().pushPose();
            g.pose().translate((float) x0, (float) titleY, 0f);
            g.pose().scale(titleScale, titleScale, 1f);
            g.drawString(font, Component.translatable("survivorauth.title"), 0, 0, titleColor, false);
            g.pose().popPose();
        }

        // traço de destaque sob o título
        g.fill(x0, accentY, x0 + 32, accentY + 2, accent);

        g.drawString(font, Component.translatable(
                registerMode ? "survivorauth.subtitle.register" : "survivorauth.subtitle.login"), x0, subY, dim, false);

        String name = Minecraft.getInstance().getUser().getName();
        g.drawString(font, Component.translatable("survivorauth.id", name), x0, idY, textColor, false);

        g.drawString(font, Component.translatable("survivorauth.label.password"), x0, l1Y, dim, false);
        if (registerMode) {
            g.drawString(font, Component.translatable("survivorauth.label.confirm"), x0, l2Y, dim, false);
        }

        // mensagem de status (até 2 linhas)
        List<FormattedCharSequence> lines = font.split(status, fieldW);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            g.drawString(font, lines.get(i), x0, statusY + i * 10, 0xFFD9604A, false);
        }

        // rodapé (esquerda) e contagem regressiva (direita), no canto da tela
        g.drawString(font, Component.translatable("survivorauth.footer"), x0, height - 18, withAlpha(dim, 0xB0), false);

        long left = Math.max(0L, (endTimeMs - System.currentTimeMillis()) / 1000L);
        String time = String.format(Locale.ROOT, "%02d:%02d", left / 60, left % 60);
        boolean blinkOff = left <= 15 && (System.currentTimeMillis() / 400) % 2 == 0;
        Component timer = Component.translatable("survivorauth.timer", time);
        g.drawString(font, timer, width - 24 - font.width(timer), height - 18,
                blinkOff ? 0xFFFF4040 : withAlpha(textColor, 0xC0), false);
    }

    // ------------------------------------------------------------------ botão no estilo da tela

    private final class DayButton extends AbstractWidget {
        private final int style;
        private final Runnable action;

        DayButton(int x, int y, int w, int h, Component message, int style, Runnable action) {
            super(x, y, w, h, message);
            this.style = style;
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            boolean hot = active && isHoveredOrFocused();
            int ty = getY() + (height - 8) / 2;

            if (style == STYLE_PRIMARY) {
                int fill = !active ? withAlpha(dim, 0x80) : hot ? lighten(accent) : accent;
                g.fill(getX(), getY(), getX() + width, getY() + height, fill);
                g.drawCenteredString(font, getMessage(), getX() + width / 2, ty,
                        active ? 0xFFF2EEE4 : withAlpha(textColor, 0x90));
                return;
            }

            int color = hot ? textColor : dim;
            int tw = font.width(getMessage());
            int tx = style == STYLE_TEXT_LEFT ? getX() : getX() + (width - tw) / 2;
            g.drawString(font, getMessage(), tx, ty, color, false);
            if (hot) g.fill(tx, ty + 9, tx + tw, ty + 10, color);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (active) action.run();
        }

        @Override
        public boolean keyPressed(int key, int scanCode, int modifiers) {
            boolean press = key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_SPACE;
            if (active && visible && press) {
                action.run();
                return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, getMessage());
        }
    }
}