package icy.lib.client.draw;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public final class Gfx {

    private Gfx() {}

    public static void blit(GuiGraphics g, ResourceLocation tex, int x, int y, int u, int v, int w, int h, int texW, int texH) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(tex, x, y, (float) u, (float) v, w, h, texW, texH);
        RenderSystem.disableBlend();
    }

    public static void push(GuiGraphics g) {
        g.pose().pushPose();
    }

    public static void pop(GuiGraphics g) {
        g.pose().popPose();
    }

    public static void move(GuiGraphics g, float x, float y) {
        g.pose().translate(x, y, 0);
    }

    public static void scale(GuiGraphics g, float s) {
        g.pose().scale(s, s, 1);
    }

    public static void text(GuiGraphics g, Font font, FormattedCharSequence line, int x, int y, int color, boolean shadow) {
        g.drawString(font, line, x, y, color, shadow);
    }

    public static void text(GuiGraphics g, Font font, String line, int x, int y, int color) {
        g.drawString(font, line, x, y, color, true);
    }

    public static void item(GuiGraphics g, ItemStack stack, int x, int y) {
        g.renderItem(stack, x, y);
    }

    public static void fill(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        g.fill(x0, y0, x1, y1, color);
    }

    public static void tooltip(GuiGraphics g, Font font, ItemStack stack, int x, int y) {
        g.renderTooltip(font, stack, x, y);
    }
}
