package com.survivorauth.client;

import java.io.InputStream;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Funções de desenho e cores compartilhadas pelas telas do mod (só cliente). */
final class Scenery {
    record Tex(ResourceLocation loc, int w, int h) {}

    /** Devolve a textura se existir (do mod ou de um resource pack), senão null. */
    static Tex probe(ResourceLocation loc) {
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

    /** Desenha a imagem cobrindo o retângulo inteiro, sem distorcer (corta o que sobra). */
    static void drawCover(GuiGraphics g, Tex t, int x, int y, int w, int h) {
        float scale = Math.max((float) w / t.w(), (float) h / t.h());
        int dw = Math.round(t.w() * scale);
        int dh = Math.round(t.h() * scale);
        g.enableScissor(x, y, x + w, y + h);
        g.blit(t.loc(), x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, 0f, 0f, t.w(), t.h(), t.w(), t.h());
        g.disableScissor();
    }

    /** Desenha a imagem inteira dentro do retângulo, sem distorcer (sobra espaço nas laterais). */
    static void drawContain(GuiGraphics g, Tex t, int x, int y, int w, int h) {
        float scale = Math.min((float) w / t.w(), (float) h / t.h());
        int dw = Math.round(t.w() * scale);
        int dh = Math.round(t.h() * scale);
        g.blit(t.loc(), x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, 0f, 0f, t.w(), t.h(), t.w(), t.h());
    }

    /** Cenário procedural: céu nublado, floresta de pinheiros em duas camadas e neblina. */
    static void drawWilderness(GuiGraphics g, int w, int h) {
        g.fillGradient(0, 0, w, h * 80 / 100, 0xFF1A1D18, 0xFF4B5247);
        drawPines(g, w, h * 80 / 100, Math.max(12, h * 20 / 100), 11, 3, 0xFF262C22);
        g.fillGradient(0, h * 58 / 100, w, h * 84 / 100, 0x00000000, 0x77434B3E);
        g.fillGradient(0, h * 80 / 100, w, h, 0xFF181C14, 0xFF070806);
        drawPines(g, w, h + h / 50, Math.max(20, h * 36 / 100), 26, 7, 0xFF0A0C09);
    }

    static void drawPines(GuiGraphics g, int w, int baseY, int treeH, int spacing, int seed, int color) {
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

    static int parseColor(String value, int fallback) {
        try {
            String h = value.trim();
            if (h.startsWith("#")) h = h.substring(1);
            return 0xFF000000 | (Integer.parseInt(h, 16) & 0xFFFFFF);
        } catch (Exception e) {
            return fallback;
        }
    }

    static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    static int lighten(int c) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        r += (255 - r) / 5;
        g += (255 - g) / 5;
        b += (255 - b) / 5;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private Scenery() {}
}
