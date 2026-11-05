package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class Layer {
    static final int PASS = 0;
    static final int BLOCK = 1;
    static final int DONE = 2;

    private final Map<String, Set<String>> when;
    private final Map<String, Set<String>> unless;

    Layer(JsonObject o) {
        this.when = Json.when(o, "when");
        this.unless = Json.when(o, "unless");
    }

    static Layer read(JsonObject o) {
        String type = GsonHelper.getAsString(o, "type");
        return switch (type) {
            case "image" -> new ImageLayer(o);
            case "button" -> new ButtonLayer(o);
            case "choice" -> new ChoiceLayer(o);
            case "list" -> new ListLayer(o);
            case "text" -> new TextLayer(o);
            case "items" -> new ItemsLayer(o);
            case "recipe" -> new RecipeLayer(o);
            default -> throw new JsonParseException("unknown layer type " + type);
        };
    }

    boolean shown(BookScreen s) {
        for (Map.Entry<String, Set<String>> e : when.entrySet()) {
            if (!e.getValue().contains(s.get(e.getKey()))) {
                return false;
            }
        }
        for (Map.Entry<String, Set<String>> e : unless.entrySet()) {
            if (e.getValue().contains(s.get(e.getKey()))) {
                return false;
            }
        }
        return true;
    }

    void settle(BookScreen s) {}

    abstract void render(BookScreen s, GuiGraphicsExtractor g);

    int click(BookScreen s, double ax, double ay) {
        return PASS;
    }

    void outline(BookScreen s, GuiGraphicsExtractor g) {}

    void domain(List<Entry> entries, Map<String, Set<String>> out) {}

    void names(Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {}

    void scales(Set<Float> out) {}
}
