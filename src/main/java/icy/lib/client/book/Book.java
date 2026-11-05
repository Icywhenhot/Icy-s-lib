package icy.lib.client.book;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import icy.lib.IcysLib;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

final class Book {
    final Identifier id;
    final int width;
    final int height;
    final int[] bleed;
    final float size;
    final float textScale;
    final int textColor;
    final boolean textShadow;
    final Bend bend;
    final List<Layer> layers = new ArrayList<>();
    final List<Entry> entries = new ArrayList<>();
    final int step;
    private final Identifier root;
    private final Map<String, Piece> pieces = new HashMap<>();

    Book(Identifier id, JsonObject json, Map<String, JsonObject> entryFiles, ResourceManager rm) throws IOException {
        this.id = id;
        int[] canvas = Json.ints(json, "canvas", 2, null);
        if (canvas == null) {
            throw new JsonParseException("missing canvas");
        }
        this.width = canvas[0];
        this.height = canvas[1];
        this.bleed = Json.ints(json, "bleed", 4, new int[]{0, 0, 0, 0});
        this.size = GsonHelper.getAsFloat(json, "size", 1f);
        this.root = Identifier.parse(GsonHelper.getAsString(json, "textures"));
        JsonObject text = GsonHelper.getAsJsonObject(json, "text", new JsonObject());
        this.textScale = GsonHelper.getAsFloat(text, "scale", 1f);
        Integer col = Json.color(text, "color");
        this.textColor = col == null ? 0xFF3F2F1F : col;
        this.textShadow = GsonHelper.getAsBoolean(text, "shadow", false);
        this.bend = Bend.read(json, height);

        for (JsonElement el : GsonHelper.getAsJsonArray(json, "layers")) {
            layers.add(Layer.read(el.getAsJsonObject()));
        }
        for (Map.Entry<String, JsonObject> f : entryFiles.entrySet()) {
            entries.add(Entry.read(f.getKey(), f.getValue()));
        }
        entries.sort(Comparator.comparing((Entry e) -> e.category).thenComparingInt(e -> e.sort).thenComparing(e -> e.name));

        Map<String, Set<String>> domain = new HashMap<>();
        for (Layer l : layers) {
            l.domain(entries, domain);
        }
        Set<String> need = new LinkedHashSet<>();
        Set<String> maybe = new LinkedHashSet<>();
        for (Layer l : layers) {
            l.names(domain, need, maybe);
        }
        for (Entry e : entries) {
            if (e.iconTexture != null) {
                need.add(e.iconTexture);
            }
        }
        maybe.addAll(need);
        for (String name : maybe) {
            Identifier rl = texture(name);
            Optional<Resource> res = rm.getResource(rl);
            if (res.isPresent()) {
                pieces.put(name, Piece.read(rl, res.get()));
            } else if (need.contains(name)) {
                IcysLib.LOGGER.warn("book {} is missing texture {}", id, rl);
            }
        }

        Set<Float> scales = new HashSet<>();
        scales.add(textScale);
        for (Layer l : layers) {
            l.scales(scales);
        }
        this.step = step(scales);
    }

    private static int step(Set<Float> scales) {
        for (int n = 1; n <= 64; n++) {
            boolean whole = true;
            for (float s : scales) {
                float v = n * s;
                if (Math.abs(v - Math.round(v)) > 1.0e-3f) {
                    whole = false;
                    break;
                }
            }
            if (whole) {
                return n;
            }
        }
        return 1;
    }

    Identifier texture(String name) {
        if (name.indexOf(':') >= 0) {
            return Identifier.parse(name.endsWith(".png") ? name : name + ".png");
        }
        return Identifier.fromNamespaceAndPath(root.getNamespace(), root.getPath() + name + ".png");
    }

    Piece piece(String name) {
        return pieces.get(name);
    }

    Collection<Piece> pieces() {
        return pieces.values();
    }

    Entry entry(String category, String name) {
        for (Entry e : entries) {
            if (e.category.equals(category) && e.name.equals(name)) {
                return e;
            }
        }
        return null;
    }

    List<Entry> find(String category, Map<String, String> where, UnaryOperator<String> fill, int max) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : entries) {
            if (!e.category.equals(category)) {
                continue;
            }
            boolean ok = true;
            for (Map.Entry<String, String> w : where.entrySet()) {
                if (!e.field(w.getKey()).equals(fill.apply(w.getValue()))) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                out.add(e);
            }
            if (out.size() >= max) {
                break;
            }
        }
        return out;
    }
}
