package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.GsonHelper;

import java.util.Map;
import java.util.Set;

class TextLayer extends Layer {
    private final String text;
    private final String fallback;
    private final int[] area;
    private final String align;
    private final String valign;
    private final Integer color;
    private final float scale;
    private final Sprite on;
    private final boolean bend;

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
    }

    @Override
    void render(BookScreen s, GuiGraphicsExtractor g) {
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
        s.text(g, s.lang(text, fallback), x, y, area[2], area[3], align, valign, color, scale, bend);
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
