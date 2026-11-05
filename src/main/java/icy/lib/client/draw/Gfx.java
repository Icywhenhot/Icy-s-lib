package icy.lib.client.draw;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public final class Gfx {

    private Gfx() {}

    public static void blit(GuiGraphicsExtractor g, Identifier tex, int x, int y, int u, int v, int w, int h, int texW, int texH) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, (float) u, (float) v, w, h, texW, texH);
    }

    public static void push(GuiGraphicsExtractor g) {
        g.pose().pushMatrix();
    }

    public static void pop(GuiGraphicsExtractor g) {
        g.pose().popMatrix();
    }

    public static void move(GuiGraphicsExtractor g, float x, float y) {
        g.pose().translate(x, y);
    }

    public static void scale(GuiGraphicsExtractor g, float s) {
        g.pose().scale(s, s);
    }

    public static void text(GuiGraphicsExtractor g, Font font, FormattedCharSequence line, int x, int y, int color, boolean shadow) {
        g.text(font, line, x, y, color, shadow);
    }

    public static void text(GuiGraphicsExtractor g, Font font, String line, int x, int y, int color) {
        g.text(font, line, x, y, color, true);
    }

    public static void item(GuiGraphicsExtractor g, ItemStack stack, int x, int y) {
        g.item(stack, x, y);
    }

    public static void fill(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        g.fill(x0, y0, x1, y1, color);
    }

    public static void tooltip(GuiGraphicsExtractor g, Font font, ItemStack stack, int x, int y) {
        g.setTooltipForNextFrame(font, stack, x, y);
    }
}
