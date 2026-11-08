package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.Set;

class TextLayer extends Layer {
    private static final int LINES_PER_NOTCH = 2;

    private final String text;
    private final String fallback;
    private final int[] area;
    private final String align;
    private final String valign;
    private final Integer color;
    private final float scale;
    private final Sprite on;
    private final boolean bend;
    private final Scrollbar scrollbar;

    TextLayer(JsonObject o) {
        super(o);
        this.text = GsonHelper.getAsString(o, "text");
        this.fallback = GsonHelper.getAsString(o, "fallback", null);
        this.area = Json.ints(o, "area", 4, null);
        if (area == null) {
            throw new JsonParseException("text needs an area");
        }
        this.align = GsonHelper.getAsString(o, "align", "left");
        this.valign = GsonHelper.getAsString(o, "valign", "top");
        this.color = Json.color(o, "color");
        this.scale = GsonHelper.getAsFloat(o, "scale", 0f);
        this.on = Sprite.read(o, "on");
        this.bend = GsonHelper.getAsBoolean(o, "bend", true);
        this.scrollbar = Scrollbar.read(o);
    }

    private int maxSkip(BookScreen s, String key) {
        return Math.max(0, s.lineCount(key, area[2], scale) - s.lineRoom(area[3], scale));
    }

    @Override
    void render(BookScreen s, GuiGraphics g) {
        int x = area[0];
        int y = area[1];
        if (on != null) {
            float[] b = on.box(s, 0, 0);
            if (b == null) {
                return;
            }
            x += (int) b[0];
            y += (int) b[1];
        }
        String key = s.lang(text, fallback);
        if (scrollbar == null) {
            s.text(g, key, x, y, area[2], area[3], align, valign, color, scale, bend);
            return;
        }
        int skip = Mth.clamp(s.scrolled(key), 0, maxSkip(s, key));
        s.text(g, key, x, y, area[2], area[3], align, valign, color, scale, bend, skip);
        scrollbar.render(g, skip, s.lineRoom(area[3], scale), s.lineCount(key, area[2], scale));
    }

    @Override
    boolean scroll(BookScreen s, double ax, double ay, int notches) {
        if (scrollbar == null) {
            return false;
        }
        boolean overtext = ax >= area[0] && ay >= area[1] && ax < area[0] + area[2] && ay < area[1] + area[3];
        if (!overtext && !scrollbar.over(ax, ay)) {
            return false;
        }
        String key = s.lang(text, fallback);
        s.scrollTo(key, Mth.clamp(s.scrolled(key) - notches * LINES_PER_NOTCH, 0, maxSkip(s, key)));
        return true;
    }

    @Override
    void names(Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {
        if (on != null) {
            on.names(domain, need, maybe);
        }
    }

    @Override
    void scales(Set<Float> out) {
        if (scale > 0) {
            out.add(scale);
        }
    }
}
