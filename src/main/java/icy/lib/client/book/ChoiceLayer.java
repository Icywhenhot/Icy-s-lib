package icy.lib.client.book;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ChoiceLayer extends Layer {
    private final String key;
    private final String def;
    private final boolean toggle;
    private final boolean drawAll;
    private final float[] lift;
    private final String sound;
    private final Map<String, Sprite> options = new LinkedHashMap<>();
    private final String category;
    private final Map<String, String> where;
    private final Sprite option;
    private final Map<String, Sprite> bound = new HashMap<>();

    ChoiceLayer(JsonObject o) {
        super(o);
        this.key = GsonHelper.getAsString(o, "key");
        this.def = GsonHelper.getAsString(o, "default", null);
        this.toggle = GsonHelper.getAsBoolean(o, "toggle", false);
        String draw = GsonHelper.getAsString(o, "draw", "all");
        if (!draw.equals("all") && !draw.equals("selected")) {
            throw new JsonParseException("draw must be all or selected");
        }
        this.drawAll = draw.equals("all");
        this.lift = Json.floats(o, "lift", new float[]{0, 0});
        this.sound = GsonHelper.getAsString(o, "sound", null);
        this.category = GsonHelper.getAsString(o, "category", null);
        this.where = Json.where(o);
        this.option = Sprite.read(o, "option");
        for (Map.Entry<String, JsonElement> e : GsonHelper.getAsJsonObject(o, "options", new JsonObject()).entrySet()) {
            options.put(e.getKey(), Sprite.read(e.getValue()));
        }
        if (category != null && option == null) {
            throw new JsonParseException("choice " + key + " reads " + category + " but has no option sprite");
        }
        if (category == null && options.isEmpty()) {
            throw new JsonParseException("choice " + key + " has no options");
        }
    }

    private Map<String, Sprite> options(BookScreen s) {
        if (category == null) {
            return options;
        }
        Map<String, Sprite> out = new LinkedHashMap<>();
        for (Entry e : s.book.find(category, where, s::fill, Integer.MAX_VALUE)) {
            out.put(e.name, bound.computeIfAbsent(e.name, n -> option.bind(key, n)));
        }
        return out;
    }

    @Override
    void settle(BookScreen s) {
        Map<String, Sprite> opts = options(s);
        String cur = s.raw(key);
        if (cur != null && (opts.containsKey(cur) || (toggle && cur.isEmpty()))) {
            return;
        }
        if (def != null && (opts.containsKey(def) || def.isEmpty())) {
            s.set(key, def);
        } else {
            s.set(key, toggle || opts.isEmpty() ? "" : opts.keySet().iterator().next());
        }
    }

    private float amount(BookScreen s, String value, boolean on) {
        if (lift[0] == 0 && lift[1] == 0) {
            return on ? 1f : 0f;
        }
        return s.lift.get(key + ":" + value, on, 1f);
    }

    @Override
    void render(BookScreen s, GuiGraphicsExtractor g) {
        String cur = s.get(key);
        for (Map.Entry<String, Sprite> e : options(s).entrySet()) {
            boolean on = e.getKey().equals(cur);
            float t = amount(s, e.getKey(), on);
            if (!drawAll && !on) {
                continue;
            }
            e.getValue().draw(s, g, lift[0] * t, lift[1] * t);
        }
    }

    @Override
    int click(BookScreen s, double ax, double ay) {
        Map<String, Sprite> opts = options(s);
        List<String> order = new ArrayList<>(opts.keySet());
        String cur = s.get(key);
        for (int i = order.size() - 1; i >= 0; i--) {
            String value = order.get(i);
            boolean on = value.equals(cur);
            float t = on && drawAll ? s.lift.peek(key + ":" + value) : 0f;
            if (!opts.get(value).hit(s, ax, ay, lift[0] * t, lift[1] * t)) {
                continue;
            }
            if (on && !toggle) {
                return DONE;
            }
            s.set(key, on ? "" : value);
            s.click(sound);
            return DONE;
        }
        return PASS;
    }

    @Override
    void outline(BookScreen s, GuiGraphicsExtractor g) {
        for (Sprite sp : options(s).values()) {
            s.box(g, sp.box(s, 0, 0));
        }
    }

    @Override
    void domain(List<Entry> entries, Map<String, Set<String>> out) {
        Set<String> vals = out.computeIfAbsent(key, k -> new LinkedHashSet<>());
        vals.addAll(options.keySet());
        if (category != null) {
            vals.add("");
            for (Entry e : entries) {
                if (e.category.equals(category)) {
                    vals.add(e.name);
                }
            }
        }
        if (toggle) {
            vals.add("");
        }
    }

    @Override
    void names(Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {
        for (Sprite sp : options.values()) {
            sp.names(domain, need, maybe);
        }
        if (option != null) {
            option.names(domain, need, maybe);
        }
    }
}
