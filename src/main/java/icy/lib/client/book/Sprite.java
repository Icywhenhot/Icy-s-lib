package icy.lib.client.book;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.Set;

final class Sprite {
    final String name;
    final String fallback;
    final int[] region;
    final int[] at;

    private Sprite(String name, String fallback, int[] region, int[] at) {
        this.name = name;
        this.fallback = fallback;
        this.region = region;
        this.at = at;
    }

    static Sprite read(JsonElement el) {
        if (el.isJsonPrimitive()) {
            return new Sprite(el.getAsString(), null, null, null);
        }
        JsonObject o = el.getAsJsonObject();
        return new Sprite(GsonHelper.getAsString(o, "texture"), GsonHelper.getAsString(o, "fallback", null),
                Json.ints(o, "region", 4, null), Json.ints(o, "at", 2, null));
    }

    static Sprite read(JsonObject o, String key) {
        return o.has(key) ? read(o.get(key)) : null;
    }

    Sprite bind(String key, String value) {
        String tag = "${" + key + "}";
        return new Sprite(name.replace(tag, value), fallback == null ? null : fallback.replace(tag, value), region, at);
    }

    void names(Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {
        add(name, domain, need, maybe);
        if (fallback != null) {
            add(fallback, domain, need, maybe);
        }
    }

    private static void add(String raw, Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {
        if (raw.contains("${")) {
            maybe.addAll(Template.expand(raw, domain));
        } else {
            need.add(raw);
        }
    }

    Piece piece(BookScreen s) {
        Piece p = s.book.piece(s.fill(name));
        if (p == null && fallback != null) {
            p = s.book.piece(s.fill(fallback));
        }
        return p;
    }

    private int[] rect(Piece p) {
        return region != null ? region : new int[]{p.x, p.y, p.w, p.h};
    }

    private float homeX(int[] r) {
        return at != null ? at[0] : r[0];
    }

    private float homeY(int[] r) {
        return at != null ? at[1] : r[1];
    }

    void draw(BookScreen s, GuiGraphics g, float dx, float dy) {
        Piece p = piece(s);
        if (p == null) {
            return;
        }
        int[] r = rect(p);
        s.blit(g, p, r, homeX(r) + dx, homeY(r) + dy);
    }

    void drawAt(BookScreen s, GuiGraphics g, float x, float y, float scale) {
        Piece p = piece(s);
        if (p != null) {
            s.blit(g, p, rect(p), x, y, scale);
        }
    }

    boolean hit(BookScreen s, double ax, double ay, float dx, float dy) {
        Piece p = piece(s);
        if (p == null) {
            return false;
        }
        int[] r = rect(p);
        return inside(p, r, ax - homeX(r) - dx, ay - homeY(r) - dy);
    }

    boolean hitAt(BookScreen s, double ax, double ay, float x, float y, float scale) {
        Piece p = piece(s);
        return p != null && inside(p, rect(p), (ax - x) / scale, (ay - y) / scale);
    }

    float[] box(BookScreen s, float dx, float dy) {
        Piece p = piece(s);
        if (p == null) {
            return null;
        }
        int[] r = rect(p);
        return new float[]{homeX(r) + dx, homeY(r) + dy, r[2], r[3]};
    }

    float[] boxAt(BookScreen s, float x, float y, float scale) {
        Piece p = piece(s);
        if (p == null) {
            return null;
        }
        int[] r = rect(p);
        return new float[]{x, y, r[2] * scale, r[3] * scale};
    }

    private static boolean inside(Piece p, int[] r, double lx, double ly) {
        int px = Mth.floor(lx);
        int py = Mth.floor(ly);
        if (px < 0 || py < 0 || px >= r[2] || py >= r[3]) {
            return false;
        }
        return p.solid(r[0] + px, r[1] + py);
    }
}
