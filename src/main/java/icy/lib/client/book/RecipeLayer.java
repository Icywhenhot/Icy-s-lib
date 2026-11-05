package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

class RecipeLayer extends Layer {
    private final String key;
    private final String category;
    private final String entry;
    private final String field;
    private final Sprite art;
    private final int[] at;
    private final int cell;
    private final int size;
    private final int[] result;

    RecipeLayer(JsonObject o) {
        super(o);
        this.key = GsonHelper.getAsString(o, "key", null);
        this.category = GsonHelper.getAsString(o, "category");
        this.entry = GsonHelper.getAsString(o, "entry");
        this.field = GsonHelper.getAsString(o, "field", "recipe");
        this.art = Sprite.read(o, "art");
        this.at = Json.ints(o, "at", 2, null);
        this.result = Json.ints(o, "result", 2, null);
        if (at == null || result == null) {
            throw new JsonParseException("recipe needs at and result");
        }
        this.cell = GsonHelper.getAsInt(o, "cell", 9);
        this.size = GsonHelper.getAsInt(o, "size", 8);
    }

    @Override
    void settle(BookScreen s) {
        if (key == null) {
            return;
        }
        Entry e = s.book.entry(category, s.fill(entry));
        s.set(key, e != null && !e.slots(field).isEmpty() ? "yes" : "");
    }

    @Override
    void render(BookScreen s, GuiGraphicsExtractor g) {
        Entry e = s.book.entry(category, s.fill(entry));
        if (e == null) {
            return;
        }
        List<List<ItemStack>> grid = e.slots(field);
        if (grid.isEmpty()) {
            return;
        }
        if (art != null) {
            art.draw(s, g, 0, 0);
        }
        for (int i = 0; i < grid.size() && i < 9; i++) {
            List<ItemStack> opts = grid.get(i);
            if (opts.isEmpty()) {
                continue;
            }
            s.item(g, Entry.cycle(opts), at[0] + (i % 3) * cell, at[1] + (i / 3) * cell, size, true);
        }
        s.item(g, e.icon(), result[0], result[1], size, true);
    }

    @Override
    void names(Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {
        if (art != null) {
            art.names(domain, need, maybe);
        }
    }
}
