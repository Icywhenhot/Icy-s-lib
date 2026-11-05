package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.GsonHelper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ListLayer extends Layer {
    private final String key;
    private final String category;
    private final Map<String, String> where;
    private final int[] at;
    private final int[] step;
    private final int max;
    private final int columns;
    private final float scale;
    private final Sprite frame;
    private final Sprite selected;
    private final int[] iconAt;
    private final int iconSize;
    private final Sprite label;
    private final int[] labelAt;
    private final String labelText;
    private final int[] labelArea;
    private final Integer labelColor;
    private final String labelAlign;
    private final String sound;

    ListLayer(JsonObject o) {
        super(o);
        this.key = GsonHelper.getAsString(o, "key");
        this.category = GsonHelper.getAsString(o, "category");
        this.where = Json.where(o);
        this.at = Json.ints(o, "at", 2, new int[]{0, 0});
        this.step = Json.ints(o, "step", 2, null);
        if (step == null) {
            throw new JsonParseException("list " + key + " needs a step");
        }
        this.max = GsonHelper.getAsInt(o, "max", Integer.MAX_VALUE);
        this.columns = GsonHelper.getAsInt(o, "columns", 0);
        this.scale = GsonHelper.getAsFloat(o, "scale", 1f);
        this.frame = Sprite.read(o, "frame");
        this.selected = Sprite.read(o, "selected");
        if (frame == null) {
            throw new JsonParseException("list " + key + " needs a frame");
        }
        JsonObject icon = GsonHelper.getAsJsonObject(o, "icon", new JsonObject());
        this.iconAt = Json.ints(icon, "at", 2, new int[]{0, 0});
        this.iconSize = GsonHelper.getAsInt(icon, "size", 16);
        JsonObject lbl = GsonHelper.getAsJsonObject(o, "label", null);
        if (lbl != null) {
            this.label = Sprite.read(lbl, "sprite");
            this.labelAt = Json.ints(lbl, "at", 2, new int[]{0, 0});
            this.labelText = GsonHelper.getAsString(lbl, "text", null);
            this.labelArea = Json.ints(lbl, "area", 4, null);
            this.labelColor = Json.color(lbl, "color");
            this.labelAlign = GsonHelper.getAsString(lbl, "align", "left");
        } else {
            this.label = null;
            this.labelAt = null;
            this.labelText = null;
            this.labelArea = null;
            this.labelColor = null;
            this.labelAlign = null;
        }
        this.sound = GsonHelper.getAsString(o, "sound", null);
    }

    private List<Entry> rows(BookScreen s) {
        return s.book.find(category, where, s::fill, max);
    }

    private int x(int i) {
        return at[0] + step[0] * (columns > 0 ? i % columns : i);
    }

    private int y(int i) {
        return at[1] + step[1] * (columns > 0 ? i / columns : i);
    }

    @Override
    void settle(BookScreen s) {
        List<Entry> rows = rows(s);
        String cur = s.get(key);
        for (Entry e : rows) {
            if (e.name.equals(cur)) {
                return;
            }
        }
        s.set(key, rows.isEmpty() ? "" : rows.get(0).name);
    }

    @Override
    void render(BookScreen s, GuiGraphics g) {
        List<Entry> rows = rows(s);
        String cur = s.get(key);
        float sc = s.crisp(scale);
        for (int i = 0; i < rows.size(); i++) {
            Entry e = rows.get(i);
            int x = x(i);
            int y = y(i);
            boolean on = e.name.equals(cur);
            (on && selected != null ? selected : frame).drawAt(s, g, x, y, sc);
            if (!e.icon().isEmpty()) {
                s.item(g, e.icon(), x + iconAt[0] * sc, y + iconAt[1] * sc, iconSize * sc, false);
            } else if (e.iconTexture != null) {
                Piece p = s.book.piece(e.iconTexture);
                if (p != null) {
                    s.icon(g, p, x + iconAt[0] * sc, y + iconAt[1] * sc, iconSize * sc);
                }
            }
            if (on && label != null) {
                int lx = x + labelAt[0];
                int ly = y + labelAt[1];
                label.drawAt(s, g, lx, ly, 1f);
                if (labelText != null && labelArea != null) {
                    s.text(g, s.fill(labelText), lx + labelArea[0], ly + labelArea[1], labelArea[2], labelArea[3],
                            labelAlign, "middle", labelColor, 0f, false);
                }
            }
        }
    }

    @Override
    int click(BookScreen s, double ax, double ay) {
        List<Entry> rows = rows(s);
        for (int i = 0; i < rows.size(); i++) {
            int x = x(i);
            int y = y(i);
            if (!frame.hitAt(s, ax, ay, x, y, s.crisp(scale))) {
                continue;
            }
            String name = rows.get(i).name;
            if (!name.equals(s.get(key))) {
                s.set(key, name);
                s.click(sound);
            }
            return DONE;
        }
        return PASS;
    }

    @Override
    void outline(BookScreen s, GuiGraphics g) {
        List<Entry> rows = rows(s);
        for (int i = 0; i < rows.size(); i++) {
            s.box(g, frame.boxAt(s, x(i), y(i), s.crisp(scale)));
        }
    }

    @Override
    void domain(List<Entry> entries, Map<String, Set<String>> out) {
        Set<String> vals = out.computeIfAbsent(key, k -> new LinkedHashSet<>());
        vals.add("");
        for (Entry e : entries) {
            if (e.category.equals(category)) {
                vals.add(e.name);
            }
        }
    }

    @Override
    void names(Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {
        frame.names(domain, need, maybe);
        if (selected != null) {
            selected.names(domain, need, maybe);
        }
        if (label != null) {
            label.names(domain, need, maybe);
        }
    }
}
